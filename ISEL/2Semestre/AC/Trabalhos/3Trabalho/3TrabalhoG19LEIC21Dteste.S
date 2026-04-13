	.equ	INPORT_ADDRESS, 0xD000        
	.equ	OUTPORT_ADDRESS, 0xDFFF 
	
	push	lr
	mov	r0, #0xFF
	bl	outport_write
	mov	r0, #0x00
	bl	outport_write
loop:
	bl inport_read
	mov r2, #56
	and r0, r2, r0
	lsr r0, r0, #3
	ldr r3, numeros_addr
	ldrb r0, [r3, r0]
	bl outport_write
	b loop
	pop	pc

numeros_addr:
	.word numeros
	
inport_read:
	mov	r1, #INPORT_ADDRESS & 0xFF
	movt	r1, #(INPORT_ADDRESS >> 8) & 0xFF
	ldrb	r0, [r1, #0]
	mov	pc, lr

outport_write:
	mov 	r1, #OUTPORT_ADDRESS & 0xFF
	movt	r1, #(OUTPORT_ADDRESS >> 8) & 0xFF
	strb	r0, [r1, #0]
	mov	pc, lr
	
numeros:
	.byte 1, 2, 4, 8, 16, 32, 64, 128