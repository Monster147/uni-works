	.equ	pTC_ADDRESS, 0xFF40
	.equ pTC_TCR, 0
	.equ pTC_TMR, 2
	.equ pTC_TC, 4

	.equ	LED0_PIN, 1
	.equ	LED_TOGGLE_TIME, 5
	.equ	TIMER_INTERVAL, 0x0A
	.equ	OUTPORT_INIT_VALUE, 0x0		
	.equ	OUTPORT_ADDRESS, 0xFF00
	.equ	STACK_SIZE, 64 

	.section startup
	b	_start
	b	.
_start:
	ldr	sp, tos_addr
	ldr	pc, main_addr

tos_addr:
	.word	tos
main_addr:
	.word	main

	.text

main:
	mov	r0, #OUTPORT_INIT_VALUE
	bl	outport_init
	mov	r0, #TIMER_INTERVAL-1
	bl	timer_init

main_loop:
	mov	r0, #LED0_PIN
	bl	outport_clear_bits
	bl	timer_get_value
	mov	r4, r0
main_loop_led_off:
	mov	r0, r4
	bl	timer_elapsed
	mov	r1, #LED_TOGGLE_TIME
	cmp	r0, r1
	blo	main_loop_led_off

	mov	r0, #LED0_PIN
	bl	outport_set_bits
	bl	timer_get_value
	mov	r4, r0
main_loop_led_on:
	mov	r0, r4
	bl	timer_elapsed
	mov	r1, #LED_TOGGLE_TIME
	cmp	r0, r1
	blo	main_loop_led_on

	b	main_loop


; void outport_write_bits(uint8_t, uint_8)
; r0 -value
outport_init:
	ldr	r1, outport_img_addr
	strb	r0, [r1]
	ldr	r1, outport_addr
	strb	r0, [r1, #0]
	mov	pc, lr

;void timer_init( uint8_t interval )
; r0 - interval
timer_init:
	ldr	r1, ptc_addr
	mov	r2, #1
	strb	r2, [r1, #pTC_TCR]
	strb	r0, [r1, #pTC_TMR]
	mov	r2, #0
	strb	r2, [r1, #pTC_TCR]
	mov	pc, lr

;uint8_t timer_get_value(void)
timer_get_value:
	ldr	r1, ptc_addr
	ldrb	r0,  [r1, #pTC_TC]
	mov	pc, lr

ptc_addr:
	.word pTC_ADDRESS

;uint8_t timer_elapsed( uint8_t last_read )
; r0 - last_read
timer_elapsed:
	push	lr
;	push	r4
;	mov	r4, r0
	push	r0
	bl	timer_get_value
;	sub	r0, r0, r4
;	pop	r4
	pop	r1
	sub	r0, r0, r1
;	pop	lr
;	mov	pc, lr
	pop	pc

; void outport_clear_bits(uint8_t)
; r0 -pins_mask
outport_clear_bits:
	ldr	r1, outport_img_addr
	ldrb	r2, [r1]
	mvn	r0, r0
	and	r2, r2, r0
	strb	r2, [r1]
	ldr	r1, outport_addr
	strb	r2, [r1, #0]
	mov	pc, lr

; void outport_set_bits(uint8_t)
; r0 -pins_mask
outport_set_bits:
	ldr r1, outport_img_addr
	ldrb r2, [r1]
	orr r2, r2, r0
	strb r2, [r1]
	ldr r1, outport_addr
	strb r2, [r1, #0]
	mov pc, lr


	.data
	
outport_img_addr:
	.word	outport_img
outport_addr:
	.word	OUTPORT_ADDRESS
	.section .bss
outport_img:
	.space 	1


	.stack
	.space	STACK_SIZE
tos:
