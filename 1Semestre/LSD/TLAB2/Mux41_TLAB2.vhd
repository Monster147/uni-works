library ieee;

use ieee.std_logic_1164.all;

entity Mux41_TLAB2 is
port(
	A_Mux41 : in std_logic_vector(3 downto 0);
	B_Mux41 : in std_logic_vector(3 downto 0);
	C_Mux41 : in std_logic_vector(3 downto 0);
	D_Mux41 : in std_logic_vector(3 downto 0);
	S0_Mux41 : in std_logic;
	S1_Mux41 : in std_logic;
	Y_Mux41 : out std_logic_vector(3 downto 0)
	);
end Mux41_TLAB2;

architecture Mux41_TLAB2_ARCH of Mux41_TLAB2 is
component Mux_TLAB2 is
port(
	A_Mux : in std_logic_vector(3 downto 0);
	B_Mux : in std_logic_vector(3 downto 0);
	S_Mux : in std_logic;
	Y_Mux : out std_logic_vector(3 downto 0)
	);
end component;

signal Y1, Y2:std_logic_vector(3 downto 0);

begin

U1: Mux_TLAB2 port map (A_Mux => A_Mux41, B_Mux => B_Mux41, Y_Mux => Y1, S_Mux => S0_Mux41);
U2: Mux_TLAB2 port map (A_Mux => C_Mux41, B_Mux => D_Mux41, Y_Mux => Y2, S_Mux => S0_Mux41);
U3: Mux_TLAB2 port map (A_Mux => Y1, B_Mux => Y2, Y_Mux => Y_Mux41, S_Mux => S1_Mux41);
end Mux41_TLAB2_ARCH;