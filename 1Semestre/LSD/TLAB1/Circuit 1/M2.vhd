library ieee;

use ieee.std_logic_1164.all;

entity M2 is port(
	W : in std_logic;
	X : in std_logic;
	Y : in std_logic;
	Z : in std_logic;
	R : out std_logic
	);
end M2;

architecture BAH of M2 is
component CircuitoFA is port(
	A : in std_logic;
	B : in std_logic;
	C : in std_logic;
	S0 : out std_logic;
	S1 : out std_logic
	);
end component;

signal B_S0, B_S1, A_S1, S0_S0, S0_S1, S1_S2: std_logic;

begin
U1: CircuitoFA port map(A => W, B => X, C => Y, S1 => B_S1, S0 => B_S0);
U2: CircuitoFA port map(A => '0', B => B_S0, C => Z, S0 => S0_S0, S1 => A_S1);
U3: CircuitoFA port map(A => A_S1, B => B_S1, C => '0', S0 => S0_S1, S1 => S1_S2);
R <= S1_S2 or (S0_S1 and S0_S0);
end BAH;
