library ieee;
use ieee.std_logic_1164.all;

entity OutputBuffer is 
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
end OutputBuffer;

architecture OutputBuffer_ARCH of OutputBuffer is
component BufferController is
	port(
	Load: in std_logic;
	ACK: in std_logic;
	clk: in std_logic;
	reset: in std_logic;
	Wreg: out std_logic;
	OBfree: out std_logic;
	Dval: out std_logic
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

signal Wreg: std_logic;

begin
U1: BufferController port map (Load => Load, ACK => ACK, clk => MCLK, reset => RESET, Wreg => Wreg, OBfree => OBfree, Dval => Dval);
U2: Reg port map (D => D, CLK => Wreg, EN => '1', RESET => RESET, Q => Q);
end OutputBuffer_ARCH;
