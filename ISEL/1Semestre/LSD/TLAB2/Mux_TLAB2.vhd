library ieee;

use ieee.std_logic_1164.all;

entity Mux_TLAB2 is
port(
	A_Mux : in std_logic_vector(3 downto 0);
	B_Mux : in std_logic_vector(3 downto 0);
	S_Mux : in std_logic;
	Y_Mux : out std_logic_vector(3 downto 0)
	);
end Mux_TLAB2;

architecture Mux_TLAB2_ARCH of Mux_TLAB2 is
begin
Y_Mux(0) <= (B_Mux(0) and not S_MUX) or (S_Mux and A_Mux(0));--trocar b com a
Y_Mux(1) <= (B_Mux(1) and not S_MUX) or (S_Mux and A_Mux(1));
Y_Mux(2) <= (B_Mux(2) and not S_MUX) or (S_Mux and A_Mux(2));
Y_Mux(3) <= (B_Mux(3) and not S_MUX) or (S_Mux and A_Mux(3));
end Mux_TLAB2_ARCH;