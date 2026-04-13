library ieee;
use ieee.std_logic_1164.all;

entity RingBuffer is
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
end RingBuffer;

architecture RingBuffer_ARCH of RingBuffer is
component RingBufferControl is
    port(
    DAV : in std_logic;
    CTS : in std_logic;
    full : in std_logic;
    empty : in std_logic;
	 clk : in std_logic;
	 RESET : in std_logic;
	 Wr : out std_logic;
	 SELpg : out std_logic;
	 Wreg : out std_logic;
    DAC : out std_logic;
	 incPut : out std_logic;
	 incGet : out std_logic
    );
end component;
component MemoryAddressControl is
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
end component;
component RAM is
	generic(
		ADDRESS_WIDTH : natural := 3;
		DATA_WIDTH : natural := 4
	);
	port(
		address : in std_logic_vector(ADDRESS_WIDTH - 1 downto 0);
		wr: in std_logic;
		din: in std_logic_vector(DATA_WIDTH - 1 downto 0);
		dout: out std_logic_vector(DATA_WIDTH - 1 downto 0)
	);
end component;

signal full, empty, incPut, incGet, PUTget, Wr: std_logic;
signal A: std_logic_vector(2 downto 0);

begin
U1: RingBufferControl port map (DAV => DAV, CTS => CTS, full => full, empty => empty, clk => clk, RESET => RESET, Wr => Wr,
										  SELpg => PUTget, Wreg => Wreg, DAC => DAC, incPut => incPut, incGet => incGet);
U2: MemoryAddressControl port map (PUTget => PUTget, incPut => incPut, incGet => incGet, MCLK => clk, RESET => RESET,
											  A => A, full => full, empty => empty);
U3: RAM port map (address => A, wr => Wr, din => D, dout => Q);
end RingBuffer_ARCH;