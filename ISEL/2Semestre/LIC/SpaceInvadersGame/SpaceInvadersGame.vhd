library ieee;
use ieee.std_logic_1164.all;

entity SpaceInvadersGame is port (
	reset: in std_logic;
	MCLK: in std_logic;
	coin: in std_logic;
	maintenence: in std_logic;
	KEY_LINHA : in std_logic_vector(3 downto 0);
	KEY_COLUNA: out std_logic_vector(2 downto 0);
	E, RS, coinAccept: out std_logic;
	Dout: out std_logic_vector (7 downto 0);
	HEX0	: out std_logic_vector(7 downto 0);
	HEX1	: out std_logic_vector(7 downto 0);
	HEX2	: out std_logic_vector(7 downto 0);
	HEX3	: out std_logic_vector(7 downto 0);
	HEX4	: out std_logic_vector(7 downto 0);
	HEX5	: out std_logic_vector(7 downto 0)
);
end SpaceInvadersGame;

architecture SpaceInvadersGame_ARCH of SpaceInvadersGame is
component SLCDC is port (
	reset: in std_logic;
	MCLK: in std_logic;
	SS: in std_logic;
	SCLK: in std_logic;
	SDX: in std_logic;
	E, RS: out std_logic;
	Dout: out std_logic_vector (7 downto 0)
);
end component;
component SerialScoreController is port (
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
end component;
component KeyboardReader is
	port(
	MACK : in std_logic;
	KEY_LINHA : in std_logic_vector(3 downto 0);
	MCLK : in std_logic;
	MRESET : in std_logic;
	KEY_COLUNA: out std_logic_vector(2 downto 0);
	D_VAL: out std_logic;
	D: out std_logic_vector(3 downto 0)
	);
end component;
component UsbPort IS 
	PORT
	(
		inputPort:  IN  STD_LOGIC_VECTOR(7 DOWNTO 0);
		outputPort :  OUT  STD_LOGIC_VECTOR(7 DOWNTO 0)
	);
END component;

signal ACK, DX_VAL, SDXport, SCLKport, SSport, SCset_port: std_logic;
signal DX: std_logic_vector(3 downto 0);

begin
U0: UsbPort port map(inputPort(4) => DX_VAL, inputPort(3 downto 0) => DX, inputPort(6) => coin, inputPort(7) => maintenence, outputPort(7) => ACK, outputPort(6) => coinAccept, outputPort(0) => SSport, outputPort(3) => SDXport, outputPort(4) => SCLKport, outputPort(1) =>SCset_port);
U1: KeyboardReader port map (MACK => ACK, KEY_LINHA => KEY_LINHA, KEY_COLUNA => KEY_COLUNA, D_VAL => DX_VAL, D => DX, MCLK => MCLK, MRESET => reset);
U2: SLCDC port map (reset => reset, MCLK => MCLK, SS => SSport, SCLK => SCLKport, SDX => SDXport, E => E, RS => RS, Dout => Dout);
U3: SerialScoreController port map(Reset => reset, MCLK => MCLK, SCset => SCset_port, SDX => SDXport, SCLK => SCLKport,
								   HEX0 => HEX0, HEX1 => HEX1, HEX2 => HEX2, HEX3 => HEX3, HEX4 => HEX4, HEX5 => HEX5);
end SpaceInvadersGame_ARCH;
