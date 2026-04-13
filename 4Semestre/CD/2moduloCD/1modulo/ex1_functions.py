import math
def multiplo4 (left,right):
    multiplo = []
    for i in range(left, right+1):
        if i%4==0:
           multiplo.append(i)
    return multiplo

#resultados = multiplo4(1,100)
#print(resultados)

def mdc(a,b):
    while b !=0:
        resto = a % b
        a = b
        b = resto
            
    return a

#print(mdc(100,50))

def pg(N, u ,r):
    pg = []
    for i in range(N):
        pg.append(u)
        u = u*r
    return pg    
      
#print(pg(10,1,2))

def n_combinacoes(n,k):
    if (n <k): return print("n tem que ser maior que k")
    quociente = math.factorial(n)
    divisor =  math.factorial(k)*math.factorial(n-k)
    return quociente/divisor

#print(n_combinacoes(2,1))

def min_max_vetor(vetor):
    if len(vetor) == 0:
        return None, None
    maximo = vetor[0]
    minimo = vetor[0]
    for i in range(len(vetor)):
        if vetor[i] > maximo:
            maximo = vetor[i]
        if vetor[i] < minimo:
            minimo = vetor[i]
    return minimo,maximo

#print(min_max_vetor([1,2,3,4,5,6,7,8,9,10]))