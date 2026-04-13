library ieee;

use ieee.std_logic_1164.all;

entity FullAd is
 port(
 A: in std_logic;
 B: in std_logic;
 Ci: in std_logic;
 S: out std_logic;
 Co: out std_logic	
 );
end FullAd;

architecture FullAd_ARCH of FullAd is
component HalfAd is 
	port(
	A: in std_logic;
	B: in std_logic;
	S: out std_logic;
	Co: out std_logic
	);
end component;

signal AB:std_logic;
signal carry:std_logic_vector(1 downto 0);

begin
AD1: HalfAd port map ( A => A, B => B, S => AB, Co => carry(0));
AD2: HalfAd port map (A => AB, B => Ci, S=>S, Co => carry(1));
Co <= carry(0) or carry(1);
end FullAd_ARCH;