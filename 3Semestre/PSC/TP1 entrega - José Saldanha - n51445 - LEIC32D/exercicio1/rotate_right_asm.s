/*
    void rotate_right(unsigned long value[], size_t n);
*/

    .text
    .global rotate_right
rotate_right:
    push    %rbx	          # value[]
    push    %rbp	          # n
    push	%r12
    push	%r10
    
    mov     %rdi, %rbx          # value[]
    mov     %rsi, %rbp          # n
    mov 	$64, %rcx
    mov     %rcx, %r12          # r12 = rcx -> r12 = nbits
    
    # n %= nbits * 2
    shl     $1, %rcx          	# nbits * 2
    xor     %rax, %rax          # Apaga o conteúdo de rax
    mov     %rsi, %rax          # rax = n
    xor     %rdx, %rdx			# Apaga o conteúdo de rdx
    div     %rcx                # rax = n / (nbits * 2), rdx = n % (nbits * 2)
    mov     %rdx, %rax          # rax = n % (nbits * 2)
    
    # if n == 0
    test    %rdx, %rdx
    jz      rotate_right_end
    
    # if n >= nbits
    mov		$64, %rcx
    cmp     %r12, %rax			# Compara o valor de n com nbits
    mov		%rdx, %r12
    jb      if_nbitseqn
    
    mov     (%rbx), %rax        # rax = value[0]
    mov     8(%rbx), %rdx       # rdx = value[1]
    mov     %rdx, (%rbx)        # value[0] = value[1]
    mov     %rax, 8(%rbx)       # value[1] = value[0]
    cmp		%rcx, %rax
    jz 		rotate_right_end
    
if_nbitseqn:
    # if n == nbits
    cmp     %r12, %rcx
    je      rotate_right_end
    
    mov     %r12, %rcx          # cl = n 
    mov     (%rbx), %rdx        # rdx = value[0] 
    mov     (%rbx), %r10        # r10 = value[0] 
    mov     8(%rbx), %rax       # rax = value[1] 
    
    # value[0] = (value[0] >> n) + (value[1] << (nbits - n))   
    shrd    %cl, %rax, %r10
    mov     %r10, (%rbx)        
    
    # value[1] = (value[1] >> n) + (tmp << (nbits - n))
    shrd    %cl, %rdx, %rax
    mov     %rax, 8(%rbx)       
    
rotate_right_end:
	pop		%r10
	pop	   	%r12
	pop    	%rbp
    pop    	%rbx
    ret

    .section .note.GNU-stack
