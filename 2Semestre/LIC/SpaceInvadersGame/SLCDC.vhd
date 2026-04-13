library ieee;
use ieee.std_logic_1164.all;

entity SLCDC is port (
	reset: in std_logic;
	MCLK: in std_logic;
	SS: in std_logic;
	SCLK: in std_logic;
	SDX: in std_logic;
	E, RS: out std_logic;
	Dout: out std_logic_vector (7 downto 0)
);
end SLCDC;

architecture SLCDC_ARCH of SLCDC is
component SerialReceiver is port (
 	SDX: in std_logic;
	SCLK: in std_logic;
	SS: in std_logic;
	Reset: in std_logic;
	Accept: in std_logic;
	MCLK: in std_logic;
	D: out std_logic_vector(8 downto 0);
	DXval: out std_logic
);
end component;
component LCDDispatcher is port (
 	Dval: in std_logic;
	Din: in std_logic_vector(8 downto 0);
	MCLK: in std_logic;
	RESET: in std_logic;
	WrL: out std_logic;
	done: out std_logic;
	Dout: out std_logic_vector(8 downto 0)
);
end component;

signal ligDone, ligDval: std_logic;
signal ligD: std_logic_vector(8 downto 0);

begin 
U1: SerialReceiver port map(SDX => SDX, SCLK => SCLK, SS => SS, MCLK => MCLK, Reset => Reset, Accept => ligDone, D => ligD, DXval => ligDval);
U2: LCDDispatcher port map (Dval => ligDval, Din =>ligD, MCLK => MCLK, RESET=> Reset, WrL => E , done => ligDone, Dout(0) => RS, Dout(8 downto 1) => Dout);
end SLCDC_ARCH;