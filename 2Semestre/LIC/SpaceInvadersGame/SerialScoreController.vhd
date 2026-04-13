library ieee;
use ieee.std_logic_1164.all;

entity SerialScoreController is port (
	Reset: in std_logic;
	MCLK: in std_logic;
	SCset: in std_logic;
	SDX: in std_logic;
	SCLK: in std_logic;
	HEX0	: out std_logic_vector(7 downto 0);
	HEX1	: out std_logic_vector(7 downto 0);
	HEX2	: out std_logic_vector(7 downto 0);
	HEX3	: out std_logic_vector(7 downto 0);
	HEX4	: out std_logic_vector(7 downto 0);
	HEX5	: out std_logic_vector(7 downto 0)
);
end SerialScoreController;
architecture SerialScoreController_ARCH of SerialScoreController is 
component SerialReceiver7 is port (
 	SDX: in std_logic;
	SCLK: in std_logic;
	SS: in std_logic;
	Reset: in std_logic;
	Accept: in std_logic;
	MCLK: in std_logic;
	D: out std_logic_vector(6 downto 0);
	DXval: out std_logic
);
end component;
component ScoreDispatcher is port (
 	Dval: in std_logic;
	Din: in std_logic_vector(6 downto 0);
	MCLK: in std_logic;
	RESET: in std_logic;
	WrD: out std_logic;
	done: out std_logic;
	Dout: out std_logic_vector(6 downto 0)
);
end component;
component scoreDisplay is
port(	set	: in std_logic;
		cmd	: in std_logic_vector(2 downto 0);
		data	: in std_logic_vector(3 downto 0);
		HEX0	: out std_logic_vector(7 downto 0);
		HEX1	: out std_logic_vector(7 downto 0);
		HEX2	: out std_logic_vector(7 downto 0);
		HEX3	: out std_logic_vector(7 downto 0);
		HEX4	: out std_logic_vector(7 downto 0);
		HEX5	: out std_logic_vector(7 downto 0)
		);
end component;
signal ligDval, ligDone, ligSet: std_logic;
signal ligD, ligDout: std_logic_vector(6 downto 0);
begin 
U1: SerialReceiver7 port map(SDX => SDX, SCLK => SCLK, SS => SCset, MCLK => MCLK, Reset => Reset, Accept => ligDone, D => ligD, DXval => ligDval);
U2: ScoreDispatcher port map (Dval => ligDval, Din =>ligD, MCLK => MCLK, RESET=> Reset, WrD => ligSet , done => ligDone, Dout(2 downto 0) => ligDout(2 downto 0),Dout(6 downto 3) => ligDout(6 downto 3)  );
U3: scoreDisplay port map (set => ligSet, cmd=>ligDout(2 downto 0), data=> ligDout(6 downto 3), HEX0 => HEX0,HEX1 => HEX1,HEX2 => HEX2,HEX3 => HEX3,HEX4 => HEX4,HEX5 => HEX5);
end SerialScoreController_ARCH;
