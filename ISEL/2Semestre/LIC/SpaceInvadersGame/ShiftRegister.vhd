library ieee;
use ieee.std_logic_1164.all;

entity ShiftRegister is port (

	data: in std_logic;
	CLK: in std_logic;
	enableSHIFT: in std_logic;
	clear:in std_logic;
	Q: out std_logic_vector(8 downto 0)
);
 
end ShiftRegister;

architecture structural of ShiftRegister is

component FFD is port(
   CLK: in std_logic;
   RESET: in std_logic;
	SET: in std_logic;
	D: in std_logic;
	EN: in std_logic;
   Q: out std_logic
  );
end component;
signal SaidaQ: std_logic_vector(8 downto 0);
begin
U1: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>data, Q=> SaidaQ(0));
U2: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(0), Q=> SaidaQ(1));
U3: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(1), Q=> SaidaQ(2));
U4: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(2), Q=> SaidaQ(3));
U5: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(3), Q=> SaidaQ(4));
U6: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(4), Q=> SaidaQ(5));
U7: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(5), Q=> SaidaQ(6));
U8: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(6), Q=> SaidaQ(7));
U9: FFD port map (CLK=>CLK, RESET=> Clear, SET=>'0', EN=>enableSHIFT, D=>SaidaQ(7), Q=> SaidaQ(8));
Q(0)<= SaidaQ(8);
Q(1)<= SaidaQ(7);
Q(2)<= SaidaQ(6);
Q(3)<= SaidaQ(5);
Q(4)<= SaidaQ(4);
Q(5)<= SaidaQ(3);
Q(6)<= SaidaQ(2);
Q(7)<= SaidaQ(1);
Q(8)<= SaidaQ(0);
end structural;