library ieee;
use ieee.std_logic_1164.all;

entity SerialReceiver7 is port (
 	SDX: in std_logic;
	SCLK: in std_logic;
	SS: in std_logic;
	Reset: in std_logic;
	Accept: in std_logic;
	MCLK: in std_logic;
	D: out std_logic_vector(6 downto 0);
	DXval: out std_logic
);
end SerialReceiver7;
architecture structural of SerialReceiver7 is 
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
component comparer is port (

	A: in std_logic_vector(3 downto 0);
	B: in std_logic_vector(3 downto 0);
	R: out std_logic

);
end component; 
component ShiftRegisterSC is port (

	data: in std_logic;
	CLK: in std_logic;
	enableSHIFT: in std_logic;
	clear:in std_logic;
	Q: out std_logic_vector(6 downto 0)
);
end component; 
component ParityCheck is port (

	data: in std_logic;
	CLK: in std_logic;
	init: in std_logic;
	err: out std_logic
);
end component;
component SerialControl is port(
	CLK: in std_logic;
	init: out std_logic;
	wr: out std_logic;
	DX_VAL: out std_logic;
	RESET: in std_logic;
	accept: in std_logic;
	pFlag: in std_logic;
	dFlag: in std_logic;
	RXerror: in std_logic;
	enRX: in std_logic
);
end component;

signal WRSaida, initSaida, errSaida, pFlagSaida, dFLagSaida, saida9, saida10: std_logic;
signal SaidaQ: std_logic_vector(3 downto 0);
begin
U1: ShiftRegisterSC port map (data=>SDX, CLK=>SCLK, enableSHIFT=>WRSaida, clear=> Reset, Q=>D);
U2: Comparer port map (A=>"0111", B=>SaidaQ, R=>dFlagSaida);
U3: Comparer port map (A=>"1000", B=>SaidaQ, R=>pFLagSaida);
U4: ParityCheck port map (data=>SDX, CLK=>SCLK, init => initSaida, err=>errSaida);
U5: SerialControl port map (CLK=>MCLK,init=>initSaida, wr=>WRSaida, DX_Val=>DXval,RESET=>Reset, accept=>Accept, dFlag=>saida9, pFlag=>saida10, RXerror=>errSaida, enRX=>SS);
U6: CounterUp port map (dataIn=>"0000", PL=>'0', CE=>'1', CLK=>SCLK, RESET=>initSaida, Q=>SaidaQ);
saida9 <= dFlagSaida and (not SCLK);
saida10 <= pFLagSaida and (not SCLK);
end structural;