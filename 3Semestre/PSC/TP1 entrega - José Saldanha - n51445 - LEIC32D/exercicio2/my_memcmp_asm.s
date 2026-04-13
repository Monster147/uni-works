/*
int my_memcmp(const void *ptr1, const void *ptr2, size_t num);
*/
	.global my_memcmp
my_memcmp:
	push 	%rbx 					# *ptr1
	push 	%rbp 					# *ptr2
	push 	%r12 					# num
	push 	%r13					# i
	mov		%rdi, %rbx
	mov		%rsi, %rbp
	mov		%rdx, %r12
	xor		%r13, %r13
	test	%r12, %r12
	jz 		my_memcmp_end
loop_64:
	cmp		$8, %r12
	jl 		no_remaining_bytes		# Caso num<8, vai para a comparação de bytes
	mov 	(%rbx, %r13), %rdi					
	mov		(%rbp, %r13), %rsi		
	cmp		%rsi, %rdi 				# Comparação dos 64 bits de ptr2 com ptr1	
	jne 	diff_found_64			# Caso sejam diferentes, retorna a diferença
	add 	$8, %r13				# Adiciona +8 ao índice para ir para a próxima palavra de 64 bits
	sub 	$8, %r12				# Subtrai -8 ao tamanho, uma vez que já analisou uma palavra de 64 bits
	jmp 	loop_64
no_remaining_bytes:
	test	%r12, %r12				# Testa de num == 0
	jz 		my_memcmp_end			# Caso seja, retorna 0
loop_bytes:
	movzxb 	(%rbx, %r13), %rdi
	movzxb	(%rbp, %r13), %rsi
	cmp 	%rdi, %rsi				# Comparação de um byte de ptr2 com ptr1
	jne 	diff_found_bytes		# Caso sejam diferentes, retorna essa diferença
	inc 	%r13					# Incremente o índice para ir para o próximo byte
	dec 	%r12					# Decrementa o tamanho, uma vez que já analisou um byte
	jnz		loop_bytes
diff_found_64:
	sub 	%si, %di				# Subtrai ao bx o valor em ax, para nos dar a diferença
	xor 	%rax, %rax
	mov 	%di, %ax
	jmp 	my_memcmp_cleanup		# Salta para o restauro dos registos callee-saved
diff_found_bytes:
	sub		%rsi, %rdi				# Subtrai ao rbx o valor em rax, para nos dar a diferença
	xor 	%rax, %rax
	mov 	%rdi, %rax
	jmp 	my_memcmp_cleanup		# Salta para o restauro dos registos callee-saved
my_memcmp_end:
	xor 	%rax, %rax				# Faz o xor em rax de rax, para retornar 0
my_memcmp_cleanup:
	pop 	%r13
	pop 	%r12
	pop 	%rbp
	pop 	%rbx
	ret

	.section	.note.GNU-stack
