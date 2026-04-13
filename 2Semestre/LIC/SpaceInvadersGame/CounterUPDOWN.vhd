library ieee;
use ieee.std_logic_1164.all;

entity CounterUpDown is
	port(
	dataIn : in std_logic_vector(3 downto 0);
	PL : in std_logic;
	CE: in std_logic;
	CLK : in std_logic;
	RESET : in std_logic;
	INCdec: in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end CounterUpDown;

architecture CounterUpDown_ARCH of CounterUpDown is
component Adder is
 port(
 A: in std_logic_vector(3 downto 0);
 B: in std_logic_vector(3 downto 0);
 Ci: in std_logic;
 S: out std_logic_vector(3 downto 0);
 Co: out std_logic
 );
end component;
component Reg is 
	port(
	D: in std_logic_vector(3 downto 0);
	CLK : in std_logic;
	EN : in std_logic;
	RESET: in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;
component Mux is
port(
	A : in std_logic_vector(3 downto 0);
	B : in std_logic_vector(3 downto 0);
	S : in std_logic;
	Y : out std_logic_vector(3 downto 0)
	);
end component;

signal QA, YQ, RA, ADSUB, Count: std_logic_vector(3 downto 0);
signal ZERO : std_logic_vector(2 downto 0);

begin
ZERO <= "000";
MUX1: Mux port map (A => "1111", B => "0001", S => INCdec, Y => ADSUB);
MUX2: Mux port map (A => "0000", B => ADSUB, S => CE, Y => Count);
AD0: Adder port map (A => QA, B => Count, Ci => '0', S => RA);
MUX0: Mux port map (A => RA, B => dataIn, S => PL, Y => YQ);
REG0: Reg port map (D => YQ, CLK => CLK, RESET => RESET, Q => QA, EN => '1');
Q <= QA;
end CounterUpDown_ARCH;
	
	