	.equ	INPORT_ADDRESS, 0xFF80       
	.equ	OUTPORT_ADDRESS, 0xFFC0
	
	push	lr
	mov	r0, #0xFF
	bl	outport_write
	mov	r0, #0x00
	bl	outport_write
loop:
	bl LEDoff
	bl inport_read
	mov r2, #7
	ldr r3, ritmos_addr
	and r0, r2, r0
	ldrb r0, [r3, r0]
	bl sleep
	bl LEDon
	bl inport_read
	mov r2, #7
	ldr r3, ritmos_addr
	and r0, r2, r0
	ldrb r0, [r3, r0]
	bl sleep
	b loop
	pop	pc
	
LEDon:
	push lr
		mov r0, #0x81
		bl 	outport_write
	pop pc

LEDoff:
	push lr
		mov r0, #0x80
		bl 	outport_write
	pop pc
	
inport_read:
	mov	r1, #INPORT_ADDRESS & 0xFF
	movt	r1, #(INPORT_ADDRESS >> 8) & 0xFF
	ldrb	r0, [r1, #0]
	mov	pc, lr
	
outport_write:
	mov	r1, #OUTPORT_ADDRESS & 0xFF
	movt	r1, #(OUTPORT_ADDRESS >> 8) & 0xFF
	strb	r0, [r1, #0]
	mov	pc, lr

sleep:
	and	r0, r0, r0
	beq	sleep_end
sleep_outer_loop:
	mov	r1, #0x3E
	movt	r1, #0x03
sleep_inner_loop:
	sub	r1, r1, #1
	bne	sleep_inner_loop
	sub	r0, r0, #1
	bne	sleep_outer_loop
sleep_end:
	mov	pc, lr

ritmos_addr:
	.word ritmos

ritmos:
	.byte 5, 10, 15, 20, 25, 30, 35, 40