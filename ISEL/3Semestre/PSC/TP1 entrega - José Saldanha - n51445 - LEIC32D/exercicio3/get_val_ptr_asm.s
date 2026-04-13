/*
struct data { short flags:6; short length:10; short vals[]; };

struct info { double ref; struct data **data; int valid; };

short *get_val_ptr(struct info items[],
                      size_t item_idx, size_t data_idx, size_t val_idx, short mask)
{
	return items[item_idx].valid
		&& val_idx < items[item_idx].data[data_idx]->length
		&& (items[item_idx].data[data_idx]->flags & mask)
			? &items[item_idx].data[data_idx]->vals[val_idx]
			: NULL;
}
*/
	.text
	.global get_val_ptr

get_val_ptr:
    push    %rbx                     	# Salva o valor de items[]
    push    %rbp                     	# Salva item_idx
    push    %r12                     	# Salva data_idx
    push    %r13                     	# Salva val_idx
    push    %r14                     	# Salva mask
    mov     %rdi, %rbx               	# rbx = items[]
    mov     %rsi, %rbp               	# rbp = item_idx
    mov     %rdx, %r12               	# r12 = data_idx
    mov     %rcx, %r13               	# r13 = val_idx
    mov     %r8, %r14                	# r14 = mask

    mov     %rbp, %rdi
    shl     $1, %rdi
    add     %rdi, %rbp

get_val_ptr_check_valid:
    lea     (%rbx, %rbp, 8), %rdi     	# rdi = items[item_idx]
    mov     16(%rdi), %eax            	# eax = items[item_idx].valid
    test    %eax, %eax               	# Verifica se valid é 0
    jz      get_val_ptr_invalid_item 	# Se for 0, pula para retorno NULL

get_val_ptr_check_index:
    mov     8(%rdi), %rax              	# Carrega items[item_idx].data
    mov     (%rax, %r12, 8), %rax    	# items[item_idx].data[data_idx]
    movzwl  (%rax), %esi             	# Carrega flags e length
    mov 	%esi, %edx
    and 	$0x3FF, %edx
    shr     $6, %edx                 	# Extrai length deslocando 6 bits para a direita
    cmp     %edx, %r13d              	# Compara length com val_idx
    jge     get_val_ptr_invalid_index 	# Se val_idx >= length, retorna NULL

get_val_ptr_check_flags:
    and     $0x3F, %esi              	# Isola os 6 bits de flags
    and     %esi, %r14d              	# Verifica se flags & mask != 0
    test    %r14d, %r14d             	# Se flags & mask == 0, pula para NULL
    jz      get_val_ptr_invalid_flags

    lea     2(%rax, %r13, 2), %rax   	# Carrega o endereço de vals[val_idx]
    jmp     get_val_ptr_return

get_val_ptr_invalid_item:
    xor 	%rax, %rax					# Limpa o conteudo de rax para retornar 0 (null)
    jmp     get_val_ptr_return

get_val_ptr_invalid_index:
    xor 	%rax, %rax					# Limpa o conteudo de rax para retornar 0 (null)
    jmp     get_val_ptr_return

get_val_ptr_invalid_flags:
    xor 	%rax, %rax					# Limpa o conteudo de rax para retornar 0 (null)
    jmp     get_val_ptr_return

get_val_ptr_return:
    pop     %r14
    pop     %r13
    pop     %r12
    pop     %rbp
    pop     %rbx
    ret


	.section	.note.GNU-stack
