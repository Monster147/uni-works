### Exercise 1

**b) Subsections 1.1.3 and 5.3.1 specify a canonical address form for virtual addresses. With 4-level mapping, canonical addresses go from 0x0000000000000000 to 0x________________ and then from 0x________________ to 0xFFFFFFFFFFFFFFFF. Complete the missing values and write the canonical ranges for 5-level mapping.**

----

**4-level mapping**

0x0000000000000000 to 0x00007FFFFFFFFFFF
and
0xFFFF800000000000 to 0xFFFFFFFFFFFFFFFF


Os bits 63 a 48 replicam o bit 47, logo o maior endereço do intervalo canónico inferior será 0x00007FFFFFFFFFFF,
 onde todos os bits menos o 47 sao 1, e o menor endereço do intervalo canónico superior será 0xFFFF800000000000,
 onde todos os bits menos o 47 sao 0.

**5-level mapping**

0x0000000000000000 to 0x007FFFFFFFFFFFFF
and
0xFF80000000000000 to 0xFFFFFFFFFFFFFFFF


Os bits 63 a 57 replicam o bit 56, logo o maior endereço do intervalo canónico inferior será 0x007FFFFFFFFFFFFF,
 onde todos os bits menos o 56 sao 1, e o menor endereço do intervalo canónico superior será 0xFF80000000000000,
 onde todos os bits menos o 56 sao 0.