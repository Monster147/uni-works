library ieee;

use ieee.std_logic_1164.all;

entity LogicMod is port(
	A_LogicMod: in std_logic_vector(3 downto 0);
	B_LogicMod: in std_logic_vector(3 downto 0);
	S0_LogicMod: in std_logic;
	S1_LogicMod: in std_logic;
	R_LogicMod: out std_logic_vector(3 downto 0);
	CY_LogicMod: out std_logic
	);
end LogicMod;

architecture LogicMod_ARCH of LogicMod is
component AND_AB is port(
	A_AND: in std_logic_vector(3 downto 0);
	B_AND: in std_logic_vector(3 downto 0);
	R_AND: out std_logic_vector(3 downto 0)
	);
end component;
component OR_AB is port(
	A_OR: in std_logic_vector(3 downto 0);
	B_OR: in std_logic_vector(3 downto 0);
	R_OR: out std_logic_vector(3 downto 0)
	);
end component;
component LSR is port(
	A_LSR: in std_logic_vector(3 downto 0);
	R_LSR: out std_logic_vector(3 downto 0);
	CY_LSR: out std_logic
	);
end component;
component ASR is port(
	A_ASR: in std_logic_vector(3 downto 0);
	R_ASR: out std_logic_vector(3 downto 0);
	CY_ASR: out std_logic
	);
end component;
component Mux41_TLAB2 is
port(
	A_Mux41 : in std_logic_vector(3 downto 0);
	B_Mux41 : in std_logic_vector(3 downto 0);
	C_Mux41 : in std_logic_vector(3 downto 0);
	D_Mux41 : in std_logic_vector(3 downto 0);
	S0_Mux41 : in std_logic;
	S1_Mux41 : in std_logic;
	Y_Mux41 : out std_logic_vector(3 downto 0)
	);
end component;
component Mux1b_Flags is
port(
	A_Mux : in std_logic;
	B_Mux : in std_logic;
	S_Mux : in std_logic;
	Y_Mux : out std_logic
	);
end component;

signal RLSR, RASR, ORR, ANDR:std_logic_vector(3 downto 0);
signal ACY, LCY:std_logic;

begin

U1: LSR port map (A_LSR => A_LogicMod, R_LSR => RLSR, CY_LSR => LCY);
U2: ASR port map (A_ASR => A_LogicMod, R_ASR => RASR, CY_ASR => ACY);
U3: OR_AB port map (A_OR => A_LogicMod, B_OR => B_LogicMod, R_OR => ORR);
U4: AND_AB port map (A_AND => A_LogicMod, B_AND => B_LogicMod, R_AND => ANDR);
U5: Mux41_TLAB2 port map (A_Mux41 => ANDR, B_Mux41 => ORR, C_Mux41 => RASR, D_Mux41 => RLSR, S0_Mux41 => S0_LogicMod, S1_Mux41 => S1_LogicMod, Y_Mux41 => R_LogicMod);
U6: Mux1b_Flags port map (A_Mux => ACY, B_Mux => LCY, S_Mux => S0_LogicMod, Y_Mux => CY_LogicMod);
end LogicMod_ARCH;
