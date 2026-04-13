library ieee;

use ieee.std_logic_1164.all;

entity LUcirc is port(
	X: in std_logic_vector(3 downto 0);
	Y: in std_logic_vector(3 downto 0);
	Op: in std_logic;
	R : out std_logic_vector(3 downto 0)
	);
end LUcirc;

architecture qqlcoisa of LUcirc is
component and_ab is port(
	a : in std_logic_vector(3 downto 0);
	b : in std_logic_vector(3 downto 0);
	r2 : out std_logic_vector(3 downto 0)
	);
end component;
component orl_ab is port(
	A : in std_logic_vector(3 downto 0);
	B : in std_logic_vector(3 downto 0);
	r1 : out std_logic_vector(3 downto 0)
	);
end component;
component mux_r_ab is port(
	a1 : in std_logic_vector(3 downto 0);
	b1 : in std_logic_vector(3 downto 0);
	s : in std_logic;
	y : out std_logic_vector(3 downto 0)
	);
end component;

signal X_tb, Y_tb, R_tb: std_logic_vector(3 downto 0);
signal OP_tb: std_logic;
signal Or_Mux: std_logic_vector(3 downto 0);
signal And_Mux: std_logic_vector(3 downto 0);

begin

U1: and_ab port map(a => X, b => Y, r2 => And_Mux);
U2: orl_ab port map(A => X, B => Y, r1 => Or_Mux);
U3: mux_r_ab port map(a1 => Or_Mux, b1 => And_Mux, s => Op, y => R);

end qqlcoisa;

	