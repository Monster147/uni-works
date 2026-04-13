Library ieee;

use ieee.std_logic_1164.all;

entity and_ab is
port(
	a : in std_logic_vector(3 downto 0);
	b : in std_logic_vector(3 downto 0);
	r2 : out std_logic_vector(3 downto 0)
	);
	end and_ab;

architecture AND4 of and_ab is
begin
r2<= a and b;

end AND4;