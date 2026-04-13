/* -----------------------------------------------------------------------------
	Ficheiro:  1Trabalho.S
	Descricao: Codigo do 1º trabalho de avaliação de AC.
	Autores:   José Saldanha Nº 51445 e Ricardo Pinto Nº 51447
	Data:      22-03-2024
 -----------------------------------------------------------------------------
*/

/* Secção de arranque: código de inicialização
 * 1. Estabelece vetores (entry point após reset e de interrupção)
 * 2. Inicia stack pointer
 * 3. Chama main
 * 4. Retém a execução do CPU após o retorno do main
 */
	.section .startup
	b		_start
	b 		.
_start:
	ldr		sp, stack_top_addr
	mov		r0, pc
	add		lr, r0, #4
	ldr		pc, main_addr
	b 		.	

stack_top_addr:
	.word stack_top
main_addr:
	.word	main
	
/* Constantes */
	.equ RED_SCALE, 2
	.equ AMBAR_SCALE, 1
	.equ GREEN_SCALE, 0
	.equ WARNING_TIME, 300
	.equ CRITICAL_TIME, 150
	.equ SIZE_COLORS, 3
	.equ SIZE_TIMES, 10
	
/* Secção com código aplicacional (programa)
 */
	.text

main: ;r4 = i, r1=&color_histogram[0], r2 = color_histogram_size, r3, r0 = temp
	; salva LR se função não folha
	push	lr
	mov 	r4, #0
	ldr 	r1, color_histogram_addr
	mov 	r2, #SIZE_COLORS
	mov 	r3, #0
while:
	cmp 	r4, r2
	bhs 	test_color_scale_jump
	add 	r4, r4, #1
	mov		r0, #0
	strb 	r3, [r1, r0]
	b 		while
test_color_scale_jump:
	ldr 	r0 , times_addr
	mov 	r2, #SIZE_TIMES
	bl 		test_color_scale
	; retorna desempilhando endereço de retorno se função não folha
	mov 	r0, #0
	pop		pc
	; retorna transferindo LR para PC se função folha

color_histogram_addr:
	.word color_histogram
times_addr:
	.word	times

;r0/r4 = &array_t[0], r1/r5= &array_c[0], r2 = array_t_size, r6 = i
test_color_scale:
	push lr
    push r4
    push r5
	push r6
    push r7
    mov r4, r0
    mov r5, r1
	mov r7, r2
    mov r6, #0
test_color_scale_loop:
    cmp r6, r7
    bhs test_color_scale_end
	mov r3 , r6
	add r3, r3, r3
	ldr r0, [r4, r3]
    bl color_scale
    lsl r0, r0, #1
    ldrb r1, [r5, r0]
    add r1, r1, #1
    strb r5, [r1, r0]
    add r6, r6, #1
    b test_color_scale_loop
test_color_scale_end:
	mov r5, r1
	pop r7
    pop r6
    pop r5
    pop r4
    pop pc
	
	
;r0 = t, r1 = c, r2, r3 = temp
color_scale:
	mov  r1, #GREEN_SCALE & 0xFF
	mov  r2, #CRITICAL_TIME & 0xFF
	mov  r3, #WARNING_TIME & 0xFF
	movt r3, #( WARNING_TIME >> 8 ) & 0xFF
if:
	cmp r0, r2
	blo else_if
	cmp r3, r0
	blo else_if
	mov r1, #AMBAR_SCALE & 0xFF
else_if:
	cmp r0, r2
	bhs color_scale_ret
	mov r1, #RED_SCALE & 0xFF
color_scale_ret:
	mov pc, lr
	
/* Dados globais do programa iniciados
 */ 
	.data

color_histogram:
	.space 3
	.align

times:
	.word 65535 , 500 , 301 , 300 , 299 , 151 , 150 , 149 , 1 , 0
	
/* Stack: pilha com dados temporários
 */
	.equ STACK_SIZE, 64
	.section .stack
	.space STACK_SIZE
stack_top:

/* Resposta às perguntas do enunciado
1.	a)   	23          		.equ RED_SCALE, 2
			24          		.equ AMBAR_SCALE, 1
			25          		.equ GREEN_SCALE, 0
			26          		.equ WARNING_TIME, 300
			27          		.equ CRITICAL_TIME, 150
			28          		.equ SIZE_COLORS, 3
			29          		.equ SIZE_TIMES, 10
	b) Para esta implementação, as definições não requerem memória uma vez que quando utilizamos a diretiva .equ, esta não ocupa espaço na memória. Apenas existe no programa em tempo de compilação.
	
2.	color_histogram:
		.space 3
		.align

	times:
		.word 65535 , 500 , 301 , 300 , 299 , 151 , 150 , 149 , 1 , 0
		
3. a) 	;r0 = t, r1 = c, r2, r3 = temp
		color_scale:
			mov  r1, #GREEN_SCALE & 0xFF
			mov  r2, #CRITICAL_TIME & 0xFF
			mov  r3, #WARNING_TIME & 0xFF
			movt r3, #( WARNING_TIME >> 8 ) & 0xFF
		if:
			cmp r0, r2
			blo else_if
			cmp r3, r0
			blo else_if
			mov r1, #AMBAR_SCALE & 0xFF
		else_if:
			cmp r0, r2
			bhs color_scale_ret
			mov r1, #RED_SCALE & 0xFF
		color_scale_ret:
		mov pc, lr
	b) Esta implementação da função color_scale ocupa 26 bytes na memória, uma vez que são utilizadas 13 instruções, em que cada uma utiliza 16 bits, ou seja, 2 bytes.
	
4.	a) É preferível utilizar o registo R4 para a varíavel i em vez do registo R3, uma vez que durante a execução do código teremos de preservar o valor desta varíavel uma vez que vamos utilizar este valor para ir buscar um elemento de um array e posteriormente vamos ter de o incrementar em 1.
	b) 	;r0/r4 = &array_t[0], r1/r5= &array_c[0], r2 = array_t_size, r6 = i
		test_color_scale:
			push lr
			push r4
			push r5
			push r6
			push r7
			mov r4, r0
			mov r5, r1
			mov r7, r2
			mov r6, #0
		test_color_scale_loop:
			cmp r6, r7
			bhs test_color_scale_end
			mov r3 , r6
			add r3, r3, r3
			ldr r0, [r4, r3]
			bl color_scale
			lsl r0, r0, #1
			ldrb r1, [r5, r0]
			add r1, r1, #1
			strb r5, [r1, r0]
			add r6, r6, #1
			b test_color_scale_loop
		test_color_scale_end:
			mov r5, r1
			pop r7
			pop r6
			pop r5
			pop r4
			pop pc
*/