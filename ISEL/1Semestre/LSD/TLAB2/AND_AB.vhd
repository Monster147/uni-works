library ieee;

use ieee.std_logic_1164.all;

entity AND_AB is port(
	A_AND: in std_logic_vector(3 downto 0);
	B_AND: in std_logic_vector(3 downto 0);
	R_AND: out std_logic_vector(3 downto 0)
	);
end AND_AB;

architecture AND_AB_ARCH of AND_AB is
begin
R_AND <= A_AND and B_AND;
end AND_AB_ARCH;