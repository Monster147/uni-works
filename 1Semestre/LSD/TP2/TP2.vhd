library ieee;
use ieee.std_logic_1164.all;

entity M_ABCD is port(
	A : in std_logic;
	B : in std_logic;
	C : in std_logic;
	D : in std_logic;
	M : out std_logic
	);
	end M_ABCD;
	
architecture structural of M_ABCD is
signal term : std_logic_vector(5 downto 0);
begin
term(0) <= A and B;
term(1) <= C or D;
term(2) <= C and D;
term(3) <= A or B;
term(4) <= term(0) and term(1);
term(5) <= term(2) and term(3);
M <= term(4) or term(5);
--M <= (A and B and (C or D)) or (C and D and (A or B));

end structural;