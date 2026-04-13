Library ieee;

use ieee.std_logic_1164.all;

entity orl_ab is
port(
	A : in std_logic_vector(3 downto 0);
	B : in std_logic_vector(3 downto 0);
	r1 : out std_logic_vector(3 downto 0)
	);
	end orl_ab;

architecture ORL4 of orl_ab is
begin

r1<= a or b;

end ORL4;