### Exercise 1

**a) Compare Figure 5-17 with Figure 5-18. The change from 4-level mapping to 5-level mapping increases the maximum size of virtual address spaces. Calculate the maximum size for each case.**

----

Cada entrada de índice usa 9 bits (512 entradas), e o deslocamento (offset) tem 12 bits.
Isto aplica-se para os dois esquemas.

**4-level mapping**

Bits para índice: 9 * 4 = 36  
Bits totais de endereço virtual = 36 + 12 = 48

Máximo de endereços = 2^48 bytes = 256 TB


**5-level mapping**

Bits para índice: 9 * 5 = 45  
Bits totais de endereço virtual = 45 + 12 = 57

Máximo de endereços = 2^57 bytes = 128 PB