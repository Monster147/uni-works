; Ficheiro:  lab03.S
; Descricao: Programa para a realização da 3a atividade laboratorial de
;            Arquitetura de Computadores.
; Autor:     Tiago M Dias (tiago.dias@isel.pt)
; Data:      18-04-2024

; Definicao dos valores dos simbolos utilizados no programa
;
	.equ	STACK_SIZE, 64                ; Dimensao do stack

; *** Início de zona para completar ***
	.equ	INPORT_ADDRESS, 0xFF80
	.equ	OUTPORT_ADDRESS, 0xFFC0
; *** Final de zona para completar  ***

; Seccao:    startup
; Descricao: Guarda o código de arranque do sistema
;
	.section startup
	ldr	sp, stack_top_addr
    mov r0, pc
    add lr, r0, #4
	ldr	pc, main_addr
    b   .

stack_top_addr:
	.word	stack_top
main_addr:
	.word	main

; Seccao:    text
; Descricao: Guarda o código do programa
;
	.text

; Rotina:    main
; Descricao: *** Para completar ***
; Entradas:  *** Para completar ***
; Saidas:    *** Para completar ***
; Efeitos:   *** Para completar ***
main:
	push	lr
	mov	r0, #0xFF
	bl	outport_write
	mov	r0, #0x00
	bl	outport_write
loop:
	
	bl ledoff
	bl inport_read
	mov r2, #7
	ldr r3, time_addr
	and r0, r2, r0
	ldrb r0, [r3, r0]
	bl sleep
	bl ledon
	bl inport_read
	mov r2, #7
	ldr r3, time_addr
	and r0, r2, r0
	ldrb r0, [r3, r0]
	bl sleep
	b loop
	pop	pc
	
time_addr: 
	.word tempo

; Rotina:    inport_read
; Descricao: *** Para completar ***
; Entradas:  -
; Saidas:    r0 - *** Para completar ***
; Efeitos:   r1 - *** Para completar ***
inport_read:
	mov	r1, #INPORT_ADDRESS & 0xFF
	movt	r1, #INPORT_ADDRESS >> 8 & 0xFF
	ldrb	r0, [r1, #0]
	mov	pc, lr

; Rotina:    outport_write
; Descricao: *** Para completar ***
; Entradas:  r0 - *** Para completar ***
; Saidas:    -
; Efeitos:   r1 - *** Para completar ***
outport_write:
	mov	r1, #OUTPORT_ADDRESS & 0xFF
	movt	r1, #OUTPORT_ADDRESS >> 8 & 0xFF
	strb	r0, [r1, #0]
	mov	pc, lr

; Rotina:    sleep
; Descricao: *** Para completar ***
; Entradas:  *** Para completar ***
; Saidas:    *** Para completar ***
; Efeitos:   *** Para completar ***
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
	
ledon:
	push lr
		mov r0, #0x81
		bl 	outport_write
	pop pc

ledoff:
	push lr
		mov r0, #0x80
		bl 	outport_write
	pop pc

; Seccao:    data
; Descricao: Guarda as variáveis globais
;
;	.data
tempo:
	.byte	5, 10, 15, 20, 25, 30, 35, 40

; Seccao:    stack
; Descricao: Implementa a pilha com o tamanho definido pelo simbolo STACK_SIZE
;
	.stack
	.space	STACK_SIZE
	
stack_top:
