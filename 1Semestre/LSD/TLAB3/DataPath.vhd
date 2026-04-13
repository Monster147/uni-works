library ieee;
use ieee.std_logic_1164.all;

entity DataPath is
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
end DataPath;

architecture DataPath_ARCH of DataPath is 
component Reg is 
	port(
	D: in std_logic_vector(3 downto 0);
	CLK : in std_logic;
	EN : in std_logic;
	RESET: in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;
component Adder is
 port(
 A: in std_logic_vector(3 downto 0);
 B: in std_logic_vector(3 downto 0);
 Ci: in std_logic;
 S: out std_logic_vector(3 downto 0);
 Co: out std_logic
 );
end component;
component LABd is 
	port(
	dataIn : in std_logic_vector(3 downto 0);
	terminalValue : in std_logic_vector(3 downto 0);
	RESET : in std_logic;
	PL : in std_logic;
	CE : in std_logic;
	CLK : in std_logic;
	TC : out std_logic
	);
end component;
component ShiftReg is 
	port(
	D: in std_logic_vector(3 downto 0);
	Sin: in std_logic;
	SPL : in std_logic;
	CLK : in std_logic;
	EN : in std_logic;
	RESET: in std_logic;
	Q : out std_logic_vector(3 downto 0);
	Sout: out std_logic
	);
end component;
component Mux1b is
port(
	A : in std_logic;
	B : in std_logic;
	S : in std_logic;
	Y : out std_logic
	);
end component;
component FFD IS
PORT(	CLK : in std_logic;
		RESET : in STD_LOGIC;
		SET : in std_logic;
		D : IN STD_LOGIC;
		EN : IN STD_LOGIC;
		Q : out std_logic
		);
END component;
component decoderHex IS
PORT (	bin: in std_logic_vector(7 downto 0);		
	clear : in std_logic;
	HEX0 : out std_logic_vector(7 downto 0);
	HEX1 : out std_logic_vector(7 downto 0);
	HEX2 : out std_logic_vector(7 downto 0)
);		
END component;
signal RMB, SSreg, sreg1, sreg2: std_logic_vector(3 downto 0);
signal Soutin, CoAMux, YDFF, QSin: std_logic;

begin

U1: Reg port map (D => Multiplicand, EN => EnDP, Q => RMB, CLK => CLK, RESET => '0');
U2: Adder port map (A => sreg1, B => RMB, Ci => '0', S => SSreg, Co => CoAMux);
U3: LABd port map(dataIn => "0000", terminalValue => "0011", RESET => RESET, PL => '0', CE => CE, CLK => CLK, TC => TC);
U4: Mux1b port map (A => CoAMux, B => '0', S => S_pl , Y => YDFF);
U5: FFD port map (CLK => CLK, RESET => RESET, SET => '0', D => YDFF, EN => EnASME, Q => QSin);   
U6: ShiftReg port map(D => SSreg, Sin => QSin, SPL => S_pl, CLK => CLK, EN => EnASME, RESET => RESET, Sout => Soutin, Q => sreg1);
U7: ShiftReg port map(D => multiplier, Sin => Soutin, SPL => S_pl, EN => EnASMD, CLK => CLK, RESET => '0', Sout => Sout, Q => sreg2);
U8: decoderHex port map(clear => Clear, bin(7 downto 4) => sreg1, bin(3 downto 0) => sreg2, HEX0 => HEX0, HEX1 => HEX1, HEX2 => HEX2);
Product(7 downto 4) <= sreg1;
Product(3 downto 0) <= sreg2;
end DataPath_ARCH;
