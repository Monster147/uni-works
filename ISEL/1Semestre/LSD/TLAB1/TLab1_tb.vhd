library IEEE;
use IEEE.std_logic_1164.all;

entity TLab1_tb is
end TLab1_tb;

architecture TLab1_tb_arch of TLab1_tb is
component TLab1 port (
	W, X, Y, Z: in std_logic;
	R: out std_logic
);
end component;

signal W_TB, X_TB, Y_TB, Z_TB, R_TB: std_logic;

begin
	U1: TLab1 port map (W => W_TB, X => X_TB, Y => Y_TB, Z => Z_TB, R => R_TB);
process begin
	W_TB <= '0';
	X_TB <= '0';
	Y_TB <= '0';
	Z_TB <= '0';
	wait for 10 ns;
	W_TB <= '1';
	Y_TB <= '1';
	wait for 10 ns;
	Z_TB <= '1';
	wait for 10 ns;
	W_TB <= '0';
	X_TB <= '1';
	wait for 10 ns;
	wait;
end process;
end TLab1_tb_arch;