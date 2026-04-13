/*
size_t array_remove_cond(void **array, size_t size,
                    int (*eval)(const void *, const void *), void *context) {
	for (void **current = array, **last = array + size; current < last; ) {
		if (eval(*current, context)) {
			memmove(current, current + 1, (last - current - 1) * sizeof(void *));
			size -= 1;
			last -= 1;
		}
		else {
			current += 1;
		}
	}
	return size;
}
*/
	.text
	.global	array_remove_cond
	.global memmove
array_remove_cond:
	push 	%rbx 							# array
	push 	%rbp 							# size
	push 	%r12 							# eval
	push 	%r13							# context
	push 	%r14
	push 	%r15
	
	mov 	%rdi, %rbx						# array
	mov 	%rsi, %rbp						# size
	mov 	%rdx, %r12						# eval
	mov 	%rcx, %r13						# context
	
	lea		(%rbx), %r14					# r14 = current = array
	lea		(%rbx, %rbp, 8), %r15			# r15 = last = array + size * 8
array_remove_cond_loop:
	cmp 	%r15, %r14						# Compara current (r14) com last (r15)
	jge 	array_remove_cond_ret			# Se current >= last, sai do loop
	
	mov		(%r14), %rdi 					# rdi = *current	
	mov 	%r13, %rsi						# rsi = context
	call 	*%r12							# Chama a função eval, que está no r12
	test 	%eax, %eax						# Testa o resultado de eval
	je 		array_remove_cond_not_remove	# Se eval retornar 0, não remove
	mov		%r14, %rdi						# rdi = current
	lea 	8(%r14), %rsi					# rsi = current + 8
	mov 	%r15, %rdx						# rdx = last
	sub 	%rsi, %rdx						# rdx = last - current - 8
	call 	memmove
	dec		%rbp
	sub		$8,	%r15
	jmp 	array_remove_cond_loop
array_remove_cond_not_remove:
	add		$8, %r14
	jmp 	array_remove_cond_loop
array_remove_cond_ret:
	mov 	%rbp, %rax						# rax = size
	pop 	%r15
	pop 	%r14
	pop 	%r13
	pop 	%r12
	pop 	%rbp
	pop 	%rbx
	ret

	.section	.note.GNU-stack

