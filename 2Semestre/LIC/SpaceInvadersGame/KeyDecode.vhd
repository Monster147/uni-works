library ieee;
use ieee.std_logic_1164.all;

entity KeyDecode is
	port(
	MK_ACK : in std_logic;
	KEY_LINHA : in std_logic_vector(3 downto 0);
	MCLK : in std_logic;
	MRESET : in std_logic;
	MK_VAL : out std_logic;
	KEY_COLUNA: out std_logic_vector(2 downto 0);
	MK: out std_logic_vector(3 downto 0)
	);
end KeyDecode;

architecture KeyDecode_ARCH of KeyDecode is
component KeyControl is port(
	CLK: in std_logic;
	K_SCAN: out std_logic;
	K_VAL: out std_logic;
	RESET: in std_logic;
	K_ACK: in std_logic;
	K_PRESS: in std_logic
);
end component;
component KeyScan is port (

	KScan: in std_logic;
	Clk: in std_logic;
	RESET: in std_logic;
	KEYPAD_LIN: in std_logic_vector(3 downto 0);
	KEYPAD_COL: out std_logic_vector(2 downto 0);	
	K: out std_logic_vector(3 downto 0);
	KPress: out std_logic
 );
end component;
component CLKDIV is
generic(div: natural := 50000000);
port ( clk_in: in std_logic;
		 clk_out: out std_logic);
end component;


signal Kpress, nCLK, clko: std_logic;
signal Kscan : std_logic;

begin
--nCLK <= not MCLK;
nCLK <= not clko;
U0: CLKDIV generic map(500000) port map( clk_in => MCLK, clk_out => clko);
U1: KeyControl port map (CLK=>MCLK, K_SCAN=>Kscan, K_VAL=>MK_VAL, RESET=>MRESET, K_ACK=>MK_ACK, K_PRESS=>Kpress);
U2: KeyScan port map (KScan=>Kscan, Clk=>nCLK, RESET=> MRESET, KEYPAD_LIN=>KEY_LINHA, KEYPAD_COL=>KEY_COLUNA, K=>MK, KPress=>Kpress);
end KeyDecode_ARCH;
	
	