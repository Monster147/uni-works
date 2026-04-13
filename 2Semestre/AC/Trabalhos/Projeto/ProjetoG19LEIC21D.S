; Ficheiro:  ProjetoG19LEIC21D.S
; Descricao: Ficheiro que implementa o Projeto da disciplina. Este projeto
;            consiste em fazer um semáforo, em que temos uma rotina de
;			 passagem de carros, passagem de pedestres e uma rotina de configuração
;			 onde podemos definir o tempo que demora a passar da rotina de passagem
;			 de pedestres para a rotina de passagem de carros
; Autor:     José Saldanha (Nº 51445) e Ricardo Pinto (Nº 51447)
; Data:      05-06-2024

; Definicao dos valores dos simbolos utilizados no programa
;
	.equ	CPSR_BIT_I, 0b010000          ; Mascara para o bit I do registo CPSR

	.equ	STACK_SIZE, 64                ; Dimensao do stack - 64 B

	; Definicoes do porto de entrada
	.equ	INPORT_ADDRESS, 0xFF80        ; Endereco do porto de entrada

	; Definicoes do porto de saida
	.equ	OUTPORT_ADDRESS, 0xFFC0       ; Endereco do porto de saida

	.equ	OUTPORT_INIT_VAL, 0           ; Valor inicial do porto de saida

	; Definicoes do circuito pTC
	.equ	PTC_ADDRESS,  0xFF40          ; Endereco do circuito pTC
	.equ	PTC_TCR, 0                    ; Deslocamento do registo TCR do pTC
	.equ	PTC_TMR, 2                    ; Deslocamento do registo TMR do pTC
	.equ	PTC_TC,  4                    ; Deslocamento do registo TC do pTC
	.equ	PTC_TIR, 6                    ; Deslocamento do registo TIR do pTC
	.equ	PTC_CMD_START, 0              ; Comando para iniciar a contagem no pTC
	.equ	PTC_CMD_STOP, 1               ; Comando para parar a contagem no pTC
	.equ	SYSCLK_FREQ, 0x63             ; Intervalo de contagem do circuito pTC
                                          ; que suporta a implementação do sysclk
	; Outras definicoes
	.equ	BLINK_TIME, 5
	.equ	CROSSING_TIME, 100
	.equ	PEDESTRIAN_BUTTON, 0x01
	.equ	CONFIG_BUTTON, 0x10
	.equ	TIMES_BUTTONS, 0xE0
	.equ	L1_RED, 0x01
	.equ	L1_GREEN, 0x02
	.equ	L1_AMBAR, 0x03
	.equ	L2_RED, 0x04
	.equ	L2_GREEN, 0x08
	.equ	L2_AMBAR, 0x0C
	.equ	L3_RED, 0x10
	.equ	L3_GREEN, 0x20
	.equ	L3_AMBAR, 0x30
	

; Seccao:    startup
; Descricao: Guarda o código de arranque do sistema
;  
	.section startup
	b	_start
	ldr	pc, isr_addr
_start:
	ldr	sp, stack_top_addr
    mov r0, pc
    add lr, r0, #4
	ldr	pc, main_addr
    b   .

stack_top_addr:
	.word	stack_top
main_addr:
	.word	main
isr_addr:
	.word	isr

; Seccao:    text
; Descricao: Guarda o código do programa
;
	.text
main:
	mov	r0, #OUTPORT_INIT_VAL
	bl	outport_init
	mov	r0, #SYSCLK_FREQ
	bl	sysclk_init
	mrs	r0, cpsr
	mov	r1, #CPSR_BIT_I
	orr	r0, r0, r1
	msr	cpsr, r0
	mov r5, #CROSSING_TIME
main_loop:
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzc config_fcn
	b else_trafic
config_fcn:
	bl config
	b main_loop
else_trafic:
	bl 	trafic
	b	main_loop

;Rotina: trafic
;Descrição: Rotina principal, onde estamos no modo de tráfego de carros. Aqui vamos sempre
;			verificando se for premido o botão de pedestres ou se vamos para o modo de
;			configuração.
trafic:
	push lr
	mov r0, #0xFF
	bl outport_clear_bits
	mov r0, #L3_GREEN
	bl outport_set_bits
car_trafic: ;acende a luz vermelha para os peões, verifica se o botão dos pedestres foi pressionado
	mov r0, #L2_RED
	bl outport_set_bits
	bl inport_read
	mov r2, #PEDESTRIAN_BUTTON
	and r0, r2, r0
	bl check_rising_edge
	beq pedestrian_cross
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzc config_jump_trafic
car_trafic_flash_on: 
	bl sysclk_get_ticks
	mov r4, r0
car_trafic_flash_on_do: ;ativa a luz amarela para os carros
	bl inport_read
	mov r2, #PEDESTRIAN_BUTTON
	and r0, r2, r0
	bl check_rising_edge
	beq pedestrian_cross
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzc config_jump_trafic
	mov r0, #L1_AMBAR
	bl outport_set_bits
	mov r1, r4
	mov r2, #BLINK_TIME
	bl check_time
	and r0, r0, r0
	blo car_trafic_flash_on_do
car_trafic_flash_off: 
	bl sysclk_get_ticks
	mov r4, r0
car_trafic_flash_off_do: ;desliga a luz amarela
	bl inport_read
	mov r2, #PEDESTRIAN_BUTTON
	and r0, r2, r0
	bl check_rising_edge
	beq pedestrian_cross
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzc config_jump_trafic
	mov r0, #L1_AMBAR
	bl outport_clear_bits
	mov r1, r4
	mov r2, #BLINK_TIME
	bl check_time
	and r0, r0, r0
	blo car_trafic_flash_off_do
	pop pc
	
config_jump_trafic:
	bl config
	b trafic
	
pedestrian_fcn:
	bl pedestrian_cross
	b trafic
	
;Rotina: pedestrian_cross
;Descrição: Rotina que é executada quando é premido o botão de passagem de pedestres.
;			 Nela é contado o tempo dado por CROSSING_TIME e só sai da rotina, quando
;			 esse mesmo tempo já ter percorrido.
pedestrian_cross: 
	bl inport_read
	mov r0, #0x0F
	bl outport_clear_bits
	mov r1, #L1_RED
	orr r0, r0, r1
	mov r1, #L2_GREEN
	orr r0, r0, r1
	bl outport_set_bits
	bl sysclk_get_ticks
	mov r4, r0
	mov r2, r5
pedestrian_cross_do: ;averigua se o tempo já decorreu ou não
	bl inport_read
	mov r3, #CONFIG_BUTTON
	and r0, r3, r0
	bzc config_jump_pedestrian
	mov r1, r4
	bl check_time
	and r0, r0, r0
	bzc pedestrian_cross_return
	bl inport_read
	mov r3, #PEDESTRIAN_BUTTON
	and r0, r3, r0
	bl check_rising_edge
	bzs pedestrian_cross_add
	b pedestrian_cross_do
pedestrian_cross_add: ;sempre que o o botão dos pedestres é pressionado, adiciona o tempo correspondente da pressão ao tempo corrente
	add r2, r2, r5
	b pedestrian_cross_do
pedestrian_cross_return:
	b trafic

config_jump_pedestrian:
	bl config
	b pedestrian_cross

;Rotina: config
;Descrição: Rotina que implementa o modo de configuração. Neste modo, podemos
;			 alterar o valor da variável CROSSING_TIME
config:
	push lr
	mov r0, #0xFF
	bl outport_clear_bits
	mov r0, #L3_AMBAR
	bl outport_set_bits
	bl sysclk_get_ticks
	mov r4, r0
config_lights: ;ativa a luz verde para os peões e a luz amarela para os carros
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzs main_loop
	mov r1, #L2_GREEN
	orr r0, r0, r1
	mov r1, #L1_AMBAR
	orr r0, r0, r1
	bl outport_set_bits
config_flash_on_do:  ;afere se as luzes precisam de ser desligadas
	mov r1, r4
	mov r2, #BLINK_TIME 
	bl check_time
	and r0, r0, r0
	blo config_lights
	bl sysclk_get_ticks
	mov r4, r0
config_flash_off:  ;desliga as luzes
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzs main_loop
	mov r0, #0x0F
	bl outport_clear_bits
	mov r1, r4
	mov r2, #BLINK_TIME 
	bl check_time
	and r0, r0, r0
	bzs config_flash_off
config_time:  ;controla o tempo da passagem dos peões
	bl inport_read
	mov r2, #CONFIG_BUTTON
	and r0, r2, r0
	bzs main_loop
	bl inport_read
	mov r2, #TIMES_BUTTONS
	and r0, r2, r0
	lsr r0, r0, #5
	add r0, r0, r0
	ldr r3, times_addr
	ldr r0, [r3, r0]
	mov r5, r0
	pop pc
	
times_addr:
	.word times

;Rotina: check_time
;Descrição: Verifica se já passou o tempo definido no registo 2, caso não tenha passado
;			mete o valor 0 no registo 0, caso já tenha passado, mete o valor 1 no registo
;			0.
check_time:
	push lr
	push r1
	bl sysclk_get_ticks
	pop r1
	sub r0, r0, r1
	cmp r0, r2
	mov r0, #0
	blo check_time_return
	mov r0, #1
check_time_return:
	pop pc

;Rotina: check_rising_edge
;Descrição: Rotina que verifica se houve uma transição ascendente do botão dos
;			pedestres
check_rising_edge:
	push r2
	mov r1, r0
	mov r0, #0
	ldr r2, last_button_addr
	bzs	check_rising_edge_return
	ldrb r3, [r2]
	and r3, r3, r3
	bzc check_rising_edge_return
	mov r0, #1
check_rising_edge_return:
	strb r1, [r2]
	pop r2
	mov pc, lr
	
last_button_addr:
	.word last_button


; Rotina:    isr
; Descricao: Incrementa o valor da variável global sysclk.
isr:
	push lr
	push r1
	push r0
	bl 	 sysclk_get_ticks
	add	 r0, r0, #1
	str	 r0, [r1]
	bl	 ptc_clr_irq
	pop  r0
	pop  r1
	pop	 lr
	movs pc, lr

; Rotina:    sysclk_init
; Descricao: Inicia uma nova contagem no periferico pTC com o intervalo de
;            contagem recebido em R0, em ticks, limpando eventuais pedidos de
;            interrupcao pendentes e iniciando com o valor zero a variavel
;            global sysclk.
;            Interface exemplo: void sysclk_init( uint8_t interval );
; Entradas:  R0 - Valor do novo intervalo de contagem, em ticks.
; Saidas:    -
; Efeitos:   Inicia a contagem no periferico a partir do valor zero, limpando
;            eventuais pedidos de interrupcao pendentes e iniciando com o
;            valor zero a variavel global sysclk
sysclk_init:
    push lr
	bl 	 ptc_init
	mov  r0, #0
	ldr	 r1, sysclk_addr
	str  r0, [r1]
	pop  pc

; Rotina:    sysclk_get_ticks
; Descricao: Devolve o valor corrente da variável global sysclk.
;            Interface exemplo: uint16_t sysclk_get_ticks ( );
sysclk_get_ticks:
	ldr r1, sysclk_addr
	ldr	r0, [r1]
	mov pc, lr
	
sysclk_addr:
	.word sysclk

; Rotina:    inport_read
; Descricao: Adquire e devolve o valor corrente do porto de entrada.
;            Interface exemplo: uint8_t inport_read( );
inport_read:
	ldr	r1, inport_addr
	ldrb	r0, [r1, #0]
	mov	pc, lr

inport_addr:
	.word	INPORT_ADDRESS

; Gestor de periférico para o porto de saída
;
; Rotina:    outport_set_bits
; Descricao: Atribui o valor logico 1 aos bits do porto de saida identificados
;            com o valor 1 em R0. O valor dos outros bits nao e alterado.
;            Interface exemplo: void outport_set_bits( uint8_t pins_mask );
; Entradas:  R0 - Mascara com a especificacao do indice dos bits a alterar.
; Saidas:    -
; Efeitos:   Altera o valor da variavel global outport_img.
outport_set_bits:
	push	lr
	ldr	r1, outport_img_addr
	ldrb	r2, [r1, #0]
	orr	r0, r2, r0
	strb	r0, [r1, #0]
	bl	outport_write
	pop	pc

; Rotina:    outport_clear_bits
; Descricao: Atribui o valor logico 0 aos bits do porto de saida identificados
;            com o valor 1 em R0. O valor dos outros bits nao e alterado.
;            Interface exemplo: void outport_clear_bits( uint8_t pins_mask );
; Entradas:  R0 - Mascara com a especificacao do indice dos bits a alterar.
; Saidas:    -
; Efeitos:   Altera o valor da variavel global outport_img.
outport_clear_bits:
	push	lr
	ldr	r1, outport_img_addr
	ldrb	r2, [r1, #0]
	mvn	r0, r0
	and	r0, r2, r0
	strb	r0, [r1]
	bl	outport_write
	pop	pc

; Rotina:    outport_init
; Descricao: Faz a iniciacao do porto de saida, nele estabelecendo o valor
;            recebido em R0.
;            Interface exemplo: void outport_init( uint8_t value );
; Entradas:  R0 - Valor a atribuir ao porto de saida.
; Saidas:    -
; Efeitos:   Altera o valor da variavel global outport_img.
outport_init:
	push	lr
	ldr	r1, outport_img_addr
	strb	r0, [r1]
	bl	outport_write
	pop	pc

outport_img_addr:
	.word	outport_img

; Rotina:    outport_write
; Descricao: Escreve no porto de saida o valor recebido em R0.
;            Interface exemplo: void outport_write( uint8_t value );
; Entradas:  R0 - valor a atribuir ao porto de saida.
; Saidas:    -
; Efeitos:   -
outport_write:
	ldr	r1, outport_addr
	strb	r0, [r1, #0]
	mov	pc, lr

outport_addr:
	.word	OUTPORT_ADDRESS

; Gestor de periférico para o Pico Timer/Counter (pTC)
;

; Rotina:    ptc_init
; Descricao: Faz a iniciacao do periférico pTC, habilitando o seu funcionamento
;            em modo continuo e com o intervalo de contagem recebido em R0, em
;            ticks.
;            Interface exemplo: void ptc_init( uint8_t interval );
; Entradas:  R0 - Valor do novo intervalo de contagem, em ticks.
; Saidas:    -
; Efeitos:   Inicia a contagem no periferico a partir do valor zero, limpando
;            o pedido de interrupcao eventualmente pendente.
ptc_init:
    push    lr
	ldr	r1, PTC_ADDR
	mov	r2, #PTC_CMD_STOP
	strb	r2, [r1, #PTC_TCR]
	strb	r0, [r1, #PTC_TMR]
    bl  ptc_clr_irq
	mov	r2, #PTC_CMD_START
	strb	r2, [r1, #PTC_TCR]
	pop pc

; Rotina:    ptc_start
; Descricao: Habilita a contagem no periferico pTC.
;            Interface exemplo: void ptc_start( );
; Entradas:  -
; Saidas:    -
; Efeitos:   -
ptc_start:
	ldr	r0, PTC_ADDR
	mov	r1, #PTC_CMD_START
	strb	r1, [r0, #PTC_TCR]
	mov	pc, lr

; Rotina:    ptc_stop
; Descricao: Para a contagem no periferico pTC.
;            Interface exemplo: void ptc_stop( );
; Entradas:  -
; Saidas:    -
; Efeitos:   O valor do registo TC do periferico e colocado a zero.
ptc_stop:
	ldr	r0, PTC_ADDR
	mov	r1, #PTC_CMD_STOP
	strb	r1, [r0, #PTC_TCR]
	mov	pc, lr

; Rotina:    ptc_get_value
; Descricao: Devolve o valor corrente da contagem do periferico pTC.
;            Interface exemplo: uint8_t ptc_get_value( );
; Entradas:  -
; Saidas:    R0 - O valor corrente do registo TC do periferico.
; Efeitos:   -
ptc_get_value:
	ldr	r1, PTC_ADDR
	ldrb	r0, [r1, #PTC_TC]
	mov	pc, lr

; Rotina:    ptc_clr_irq
; Descricao: Sinaliza o periferico pTC que foi atendido um pedido de
;            interrupção.
;            Interface exemplo: void ptc_clr_irq( );
; Entradas:  -
; Saidas:    -
; Efeitos:   -
ptc_clr_irq:
	ldr	r0, PTC_ADDR
	strb	r1, [r0, #PTC_TIR]
	mov	pc, lr

PTC_ADDR:
	.word	PTC_ADDRESS

; Seccao:    data
; Descricao: Guarda as variáveis globais
;
	.data
outport_img:
	.space	1

	.align
sysclk:
	.space	2

times:
	.word 150, 250, 350, 450, 550

last_button:
	.space 1
	.align

; Seccao:    stack
; Descricao: Implementa a pilha com o tamanho definido pelo simbolo STACK_SIZE
;
	.stack
	.space	STACK_SIZE
stack_top: