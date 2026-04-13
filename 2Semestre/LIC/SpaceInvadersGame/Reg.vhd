library ieee;
use ieee.std_logic_1164.all;

entity Reg is 
	port(
	D: in std_logic_vector(3 downto 0);
	CLK : in std_logic;
	EN : in std_logic;
	RESET: in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end Reg;

architecture Reg_ARCH of Reg is
component FFD IS
PORT(	CLK : in std_logic;
		RESET : in STD_LOGIC;
		SET : in std_logic;
		D : IN STD_LOGIC;
		EN : IN STD_LOGIC;
		Q : out std_logic
		);
END component;

begin
FFD3: FFD port map (CLK => CLK, D => D(3), SET => '0', EN => EN, RESET => RESET, Q => Q(3));
FFD2: FFD port map (CLK => CLK, D => D(2), SET => '0', EN => EN, RESET => RESET, Q => Q(2));
FFD1: FFD port map (CLK => CLK, D => D(1), SET => '0', EN => EN, RESET => RESET, Q => Q(1));
FFD0: FFD port map (CLK => CLK, D => D(0), SET => '0', EN => EN, RESET => RESET, Q => Q(0));
end Reg_ARCH;