Library ieee;

use ieee.std_logic_1164.all;

entity mux_r_ab is
port(
	a1 : in std_logic_vector(3 downto 0);
	b1 : in std_logic_vector(3 downto 0);
	s : in std_logic;
	y : out std_logic_vector(3 downto 0)
	);
	end mux_r_ab;

architecture Mux4 of mux_r_ab is
begin
y<= a1 when s='0' else b1;

end Mux4;