library ieee;
use ieee.std_logic_1164.all;

entity LABd is 
	port(
	dataIn : in std_logic_vector(3 downto 0);
	terminalValue : in std_logic_vector(3 downto 0);
	RESET : in std_logic;
	PL : in std_logic;
	CE : in std_logic;
	CLK : in std_logic;
	clear : in std_logic;
	TC : out std_logic;
	CountValue : out std_logic_vector(3 downto 0);
	SEG7 : out std_logic_vector(7 downto 0)
	);
end LABd;

architecture LABd_ARCH of LABd is
component CounterUp is
	port(
	dataIn : in std_logic_vector(3 downto 0);
	PL : in std_logic;
	CE: in std_logic;
	CLK : in std_logic;
	RESET : in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;
component Comparator is
	port(
	A : in std_logic_vector(3 downto 0);
	B : in std_logic_vector(3 downto 0);
	R : out std_logic
	);
end component;
component decoderHex is
port(	
	A: in std_logic_vector(3 downto 0);		
	clear : in std_logic;
	HEX0 : out std_logic_vector(7 downto 0));		
end component;
component CLKDIV is
generic(div: natural := 50000000);
port ( clk_in: in std_logic;
		 clk_out: out std_logic);
end component;

signal QR:std_logic_vector(3 downto 0);
signal clko:std_logic;

begin
CLKD : CLKDIV port map (clk_in => CLK, clk_out => clko);
CUp : CounterUp port map (dataIn => dataIn, PL => PL, CE => CE, CLK => clko, RESET => RESET, Q => QR);
Comp : Comparator port map (A => QR, B => terminalValue, R => TC);
Dec : decoderHex port map (A => QR, clear => clear, HEX0 => SEG7);
CountValue <= QR;
end LABd_ARCH;