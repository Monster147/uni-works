import unittest
from ex1_functions import multiplo4, mdc, pg, n_combinacoes, min_max_vetor

class TestGuidePraticalLesson(unittest.TestCase):

    def test_multiplos_de_quatro(self):
        self.assertEqual(multiplo4(1, 20), [4, 8, 12, 16, 20])
        self.assertEqual(multiplo4(0, 15), [0, 4, 8, 12])
        self.assertEqual(multiplo4(5, 5), [])

    def test_maximo_divisor_comum(self):
        self.assertEqual(mdc(48, 18), 6)
        self.assertEqual(mdc(100, 25), 25)
        self.assertEqual(mdc(7, 3), 1)

    def test_progressao_geometrica(self):
        self.assertEqual(pg(5, 2, 3), [2, 6, 18, 54, 162])
        self.assertEqual(pg(4, 1, 2), [1, 2, 4, 8])
        self.assertEqual(pg(3, 5, 1), [5, 5, 5])

    def test_combinacoes(self):
        self.assertEqual(n_combinacoes(5, 2), 10)
        self.assertEqual(n_combinacoes(6, 3), 20)
        self.assertEqual(n_combinacoes(10, 5), 252)

    def test_min_max_vetor(self):
        self.assertEqual(min_max_vetor([3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5]), (1, 9))
        self.assertEqual(min_max_vetor([0, -1, 1]), (-1, 1))
        self.assertEqual(min_max_vetor([7]), (7, 7))
        self.assertEqual(min_max_vetor([]), (None, None))

if __name__ == '__main__':
    unittest.main()