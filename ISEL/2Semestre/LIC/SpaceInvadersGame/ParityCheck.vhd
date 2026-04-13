library ieee;
use ieee.std_logic_1164.all;

entity ParityCheck is port (

	data: in std_logic;
	CLK: in std_logic;
	init: in std_logic;
	err: out std_logic
);
 
end ParityCheck;
architecture structural of ParityCheck is 
component CounterUP is port(
	dataIn : in std_logic_vector(3 downto 0);
	PL : in std_logic;
	CE: in std_logic;
	CLK : in std_logic;
	RESET : in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;
signal SaidaQ: std_logic_vector(3 downto 0);
begin  
U1: CounterUp port map (dataIn => "0000", PL=>'0', CE=> data, CLK=>CLK, RESET=> init, Q=> SaidaQ);
err <= SaidaQ(0);
end structural;
