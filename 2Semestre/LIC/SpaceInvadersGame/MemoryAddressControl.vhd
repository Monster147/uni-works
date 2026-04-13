library ieee;
use ieee.std_logic_1164.all;

entity MemoryAddressControl is
    port(
    PUTget : in std_logic;
    incPut : in std_logic;
    incGet : in std_logic;
	 MCLK : in std_logic;
	 reset: in std_logic;
	 A : out std_logic_vector(2 downto 0);
	 full : out std_logic;
	 empty : out std_logic
    );
end MemoryAddressControl;

architecture MemoryAddressControl_ARCH of MemoryAddressControl is
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
component CounterUpDown is
	port(
	dataIn : in std_logic_vector(3 downto 0);
	PL : in std_logic;
	CE: in std_logic;
	CLK : in std_logic;
	RESET : in std_logic;
	INCdec: in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;
component comparer is 
	port (
	A: in std_logic_vector(3 downto 0);
	B: in std_logic_vector(3 downto 0);
	R: out std_logic
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

signal putIndex, getIndex, compare: std_logic_vector(3 downto 0);
signal CE, INCdec, lixo: std_logic;

begin
U1: CounterUp port map (dataIn => "0000", PL => '0', CE => incPut, CLK => MCLK, RESET => reset, Q => putIndex);
U2: CounterUp port map (dataIn => "0000", PL => '0', CE => incGet, CLK => MCLK, RESET => reset, Q => getIndex);
U3: CounterUpDown port map (dataIn => "0000", PL => '0', CE => CE, INCdec => INCdec, CLK => MCLK, RESET => reset, Q => compare);
U4: comparer port map (A => "1000", B => compare, R => full);
U5: comparer port map (A => "0000", B => compare, R => empty);
U6: Mux port map (A => getIndex, B => putIndex, S => PUTget, Y(2 downto 0) => A, Y(3)=>lixo);
CE <= (incPut and (not incGet)) or ((not incPut) and incGet);
INCdec <= not incGet;
end MemoryAddressControl_ARCH;
 