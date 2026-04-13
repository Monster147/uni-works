library ieee;

use ieee.std_logic_1164.all;

entity OR_AB is port(
	A_OR: in std_logic_vector(3 downto 0);
	B_OR: in std_logic_vector(3 downto 0);
	R_OR: out std_logic_vector(3 downto 0)
	);
end OR_AB;

architecture OR_AB_ARCH of OR_AB is
begin
R_OR <= A_OR or B_OR;
end OR_AB_ARCH;