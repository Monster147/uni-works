library ieee;
use ieee.std_logic_1164.all;

entity TLAB3 is
	port(
	RESET	: in std_logic;
	MCLK	: in std_logic;
	multiplicand : in std_logic_vector(3 downto 0);
	multiplier : in std_logic_vector(3 downto 0);
	Start	: in std_logic;
	Product	: out std_logic_vector(7 downto 0);
	Rdy	: out std_logic;
	SEG71 : out std_logic_vector(7 downto 0);
	SEG72 : out std_logic_vector(7 downto 0);
	SEG73 : out std_logic_vector(7 downto 0)
	);
end TLAB3;

architecture TLBA3_ARCH of TLAB3 is
component DataPath is
	port(
	Multiplicand : in std_logic_vector(3 downto 0);
	multiplier : in std_logic_vector(3 downto 0);
	EnDP : in std_logic;
	EnASME : in std_logic;
	EnASMD: in std_logic;
	S_pl : in std_logic;
	RESET: in std_logic;
	CE: in std_logic;
	CLK: in std_logic;
	Clear: in std_logic;
	TC : out std_logic;
	Sout: out std_logic;
	Product: out std_logic_vector(7 downto 0);
	HEX0 : out std_logic_vector(7 downto 0);
	HEX1 : out std_logic_vector(7 downto 0);
	HEX2 : out std_logic_vector(7 downto 0)
);
end component;

component ASMControl is
port(
	RESET : in std_logic;
	Start : in std_Logic;
	TC : in std_logic;
	Sout : in std_logic;
	MCLK : in std_logic;
	E_M : out std_logic;
	CR : out std_logic;
	E_SD : out std_logic;
	E_SE : out std_logic;
	S_pl : out std_logic;
	CE : out std_logic;
	RDY : out std_logic;
	Clear : out std_logic
	);
end component;

signal TCDP, EnM, En_ShiftD, En_ShiftE, SPL, SDP, CEASM, CRASM, CLEAR: std_logic;

begin

U1: ASMControl port map (RESET => RESET, Start => Start, TC => TCDP, MCLK => MCLK, E_M => EnM, E_SD => En_ShiftD, E_SE => En_ShiftE, S_pl => SPL, Sout => SDP, CE => CEASM, RDY => Rdy, Clear => CLEAR, CR => CRASM);
U2: DataPath port map (Multiplicand => multiplicand, multiplier => multiplier, EnDP => EnM, EnASME => En_ShiftE, EnASMD => En_ShiftD, S_pl => SPL, RESET => CRASM, CE => CEASM, CLK => MCLK, TC => TCDP, Sout => SDP, Clear => CLEAR, Product => Product, HEX0 => SEG71, HEX1 => SEG72, HEX2 => SEG73);
end TLBA3_ARCH;
