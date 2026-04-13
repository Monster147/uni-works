library ieee;
use ieee.std_logic_1164.all;

entity ShiftReg is 
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
end ShiftReg;

architecture ShiftReg_ARCH of ShiftReg is
component FFD is
port(	
	CLK : in std_logic;
	RESET : in STD_LOGIC;
	SET : in std_logic;
	D : IN STD_LOGIC;
	EN : IN STD_LOGIC;
	Q : out std_logic
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
signal DQ, YD: std_logic_vector(3 downto 0);

begin
MUX3: Mux1b port map (A => D(3), B => Sin, S => SPL, Y => YD(3));
MUX2: Mux1b port map (A => D(2), B => DQ(3), S => SPL, Y => YD(2));
MUX1: Mux1b port map (A => D(1), B => DQ(2), S => SPL, Y => YD(1));
MUX0: Mux1b port map (A => D(0), B => DQ(1), S => SPL, Y => YD(0));
FFD3: FFD port map (CLK => CLK, D => YD(3), SET => '0', EN => EN, RESET => RESET, Q => DQ(3));
FFD2: FFD port map (CLK => CLK, D => YD(2), SET => '0', EN => EN, RESET => RESET, Q => DQ(2));
FFD1: FFD port map (CLK => CLK, D => YD(1), SET => '0', EN => EN, RESET => RESET, Q => DQ(1));
FFD0: FFD port map (CLK => CLK, D => YD(0), SET => '0', EN => EN, RESET => RESET, Q => DQ(0));
Sout <= DQ(0);
Q <= DQ;
end ShiftReg_ARCH;