library ieee;
use ieee.std_logic_1164.all;

entity KeyboardReader is
	port(
	MACK : in std_logic;
	KEY_LINHA : in std_logic_vector(3 downto 0);
	MCLK : in std_logic;
	MRESET : in std_logic;
	KEY_COLUNA: out std_logic_vector(2 downto 0);
	D_VAL: out std_logic;
	D: out std_logic_vector(3 downto 0)
	);
end KeyboardReader;

architecture KeyboardReader_ARCH of KeyboardReader is
component KeyDecode is
	port(
	MK_ACK : in std_logic;
	KEY_LINHA : in std_logic_vector(3 downto 0);
	MCLK : in std_logic;
	MRESET : in std_logic;
	MK_VAL : out std_logic;
	KEY_COLUNA: out std_logic_vector(2 downto 0);
	MK: out std_logic_vector(3 downto 0)
	);
end component;
component RingBuffer is
    port(
    D : in std_logic_vector(3 downto 0);
    DAV : in std_logic;
    CTS : in std_logic;
	 clk : in std_logic;
	 RESET : in std_logic;
    DAC : out std_logic;
    Wreg : out std_logic;
    Q : out std_logic_vector(3 downto 0)
    );
end component;
component OutputBuffer is 
	port(
	D : in std_logic_vector(3 downto 0);
	MCLK : in std_logic;
	RESET : in std_logic;
	Load : in std_logic;
	ACK : in std_logic;
	Dval : out std_logic;
	OBfree : out std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;


signal Kack, Kval, OBCTS, WLoad: std_logic;
signal KD, QD: std_logic_vector(3 downto 0);

begin
U1: KeyDecode port map(MK_ACK => Kack, KEY_LINHA => KEY_LINHA, MCLK => MCLK, MRESET => MRESET, MK_VAL => Kval, KEY_COLUNA => KEY_COLUNA, MK => KD);
U2: RingBuffer port map(D => KD, DAV => Kval, CTS => OBCTS, clk => MCLK, RESET => MRESET, DAC => Kack, Wreg => WLoad, Q => QD);
U3: OutputBuffer port map(D => QD, MCLK => MCLK, RESET => MRESET, Load => WLoad, ACK => MACK, DVal => D_VAL, OBfree => OBCTS, Q => D);
end KeyboardReader_ARCH;