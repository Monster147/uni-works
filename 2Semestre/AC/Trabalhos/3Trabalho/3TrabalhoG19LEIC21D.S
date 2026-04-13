; Ficheiro:  3TrabalhoG19LEIC21D.S
; Descricao: Programa para a realização do exercício 6 do 3º trabalho
;            de avaliação de Arquitetura de Computadores.
; Autor:     José Saldanha (Nº 51445) e Ricardo Pinto (Nº 51447)
; Data:      07-05-2024

; Definicao dos valores dos simbolos utilizados no programa
	.equ	STACK_SIZE, 64                
	.equ	INPORT_ADDRESS, 0xD000        
	.equ	OUTPORT_ADDRESS, 0xDFFF       


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
main:
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
	mov	r1, #OUTPORT_ADDRESS & 0xFF
	movt	r1, #(OUTPORT_ADDRESS >> 8) & 0xFF
	strb	r0, [r1, #0]
	mov	pc, lr

; Seccao:    data
; Descricao: Guarda as variáveis globais
;
	.data
numeros:
	.byte 1, 2, 4, 8, 16, 32, 64, 128

; Seccao:    stack
; Descricao: Implementa a pilha com o tamanho definido pelo simbolo STACK_SIZE
;
	.stack
	.space	STACK_SIZE
stack_top:
