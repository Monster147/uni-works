;r0 = &dst[0], r1 = &src[0], r2 = i
;r3 e r4 = temp
str_copy:
    push r4
	mov r4, #0
    mov r2, #0
str_copy_loop:
    ldrb r3, [r1,r2]
    cmp r3, r4
    beq str_copy_ret	
    mov r4, #0
    strb r3, [r0,r2]
    add r2, r2, #1
    b   str_copy_loop
str_copy_ret:
    strb r4, [r0, r2]
    pop r4
    mov pc, lr