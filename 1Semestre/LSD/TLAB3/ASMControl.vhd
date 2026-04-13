library ieee;
use ieee.std_logic_1164.all;

entity ASMControl is
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
end ASMControl;

architecture ASMControl_ARCH of ASMControl is

component FFD is
port(	CLK : in std_logic;
		RESET : in STD_LOGIC;
		SET : in std_logic;
		D : IN STD_LOGIC;
		EN : IN STD_LOGIC;
		Q : out std_logic
		);
end component;

signal D1, D0, Q1, Q0: std_logic;

begin
--Flip-Flop's
FFD_Q1: FFD port map(CLK => MCLK, RESET => RESET, SET => '0', D => D1, EN => '1', Q => Q1);
FFD_Q0: FFD port map(CLK => MCLK, RESET => RESET, SET => '0', D => D0, EN => '1', Q => Q0);
--Generate Next State
D1 <= (not Q1 and Q0) or (Q1 and Q0 and Start) or (Q1 and (NOT Q0) and TC);
D0 <= (Q1 and not Q0) or (not Q1 and not Q0 and Start) or (Q1 and Q0 and Start);
--Generate Outputs
RDY <= Q1 and Q0;
E_M <= (not Q1 and not Q0) or (not Q1 and Q0);
CR <= not Q1 and not Q0;
E_SD <= (not Q1 and not Q0) or (Q1 and not Q0);
E_SE <= (not Q1 and Q0 and Sout) or (Q1 and not Q0);
S_pl <= Q1 and not Q0;
CE <= not Q1 and Q0;
Clear <= (not Q1 and not Q0) or (not Q1 and Q0) or (Q1 and not Q0);
end ASMControl_ARCH;