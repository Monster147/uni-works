library ieee;
use ieee.std_logic_1164.all;

entity ASMControl_ROM is
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
end ASMControl_ROM;

architecture ASMControl_ROM_ARCH of ASMControl_ROM is

component FFD is
port(	CLK : in std_logic;
		RESET : in STD_LOGIC;
		SET : in std_logic;
		D : IN STD_LOGIC;
		EN : IN STD_LOGIC;
		Q : out std_logic
		);
end component;
component multiplicador_ROM is 
port(
		address: in std_logic_vector(4 downto 0);
		data: out std_logic_vector(9 downto 0)
		);
end component;

signal D1, D0, Q1, Q0: std_logic;
signal address: std_logic_vector(4 downto 0);
signal data: std_logic_vector(9 downto 0);

begin
--Flip-Flop's
FFD_Q1: FFD port map(CLK => MCLK, RESET => RESET, SET => '0', D => D1, EN => '1', Q => Q1);
FFD_Q0: FFD port map(CLK => MCLK, RESET => RESET, SET => '0', D => D0, EN => '1', Q => Q0);
--Generate Next State
--ROM
address <= Q1 & Q0 & Start & TC & Sout;
ROM : multiplicador_ROM port map (address => address, data => data);
D1 <= data(9);
D0 <= data(8);
--Generate Outputs
RDY <= data(7);
E_M <= data(6);
CR <= data(5);
E_SD <= data(4);
E_SE <= data(3);
S_pl <= data(2);
Clear <= data(1);
CE <= data(0);
end ASMControl_ROM_ARCH;