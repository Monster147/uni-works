library ieee;
use ieee.std_logic_1164.all;

entity MUX_4_1 is port (
  
	A : in std_logic ;
	B : in std_logic ;
	C : in std_logic ;
	D : in std_logic ;
	S : in std_logic_vector(1 downto 0);
	Y : out std_logic
	
);
end MUX_4_1;

architecture structural of MUX_4_1 is
component MUX_2_1 is port (

	A: in std_logic;
	B: in std_logic;
	S: in std_logic;
	Y: out std_logic
 );
 
end component MUX_2_1;
signal term0, term1: std_logic;
begin
U1: MUX_2_1 port map (A => A, B => B, S =>S(0), Y => term0);
U2: MUX_2_1 port map (A => C, B => D, S => S(0), Y => term1);
U3: MUX_2_1 port map (A => term0, B => term1, S => S(1), Y => Y);
end structural;
