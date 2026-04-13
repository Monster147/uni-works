library ieee;
use ieee.std_logic_1164.all;

entity comparer is port (

	A: in std_logic_vector(3 downto 0);
	B: in std_logic_vector(3 downto 0);
	R: out std_logic

);
 
end comparer;

architecture structural of comparer is
signal U1, U2, U3, U4: std_logic; 
begin
U1<= A(0) xnor B(0);
U2<= A(1) xnor B(1);
U3<= A(2) xnor B(2);
U4<= A(3) xnor B(3);
R<= U1 and U2 and U3 and U4;
end structural;