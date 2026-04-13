library ieee;

use ieee.std_logic_1164.all;

entity Flags_TLAB2 is
port(
	iOV_Flags : in std_logic;
	iCB_Flags : in std_logic;
	OP_Flags : in std_logic;
	R_Flags : in std_logic_vector(3 downto 0);
	CY_Flags : in std_logic;
	BE_Flags : out std_logic;
	oGE_Flags : out std_logic;
	Z_Flags : out std_logic;
	oOV_Flags : out std_logic;
	oCB_Flags : out std_logic
	);
end Flags_TLAB2;

architecture Flags_TLAB2_ARCH of Flags_TLAB2 is
component Mux1b_Flags is
port(
	A_Mux : in std_logic;
	B_Mux : in std_logic;
	S_Mux : in std_logic;
	Y_Mux : out std_logic
	);
end component;
signal zero:std_logic;

begin
--(not (R(3)+R(2)+R(1)+R(0))
zero <= not (R_Flags(3) or R_Flags(2) or R_Flags(1) or R_Flags(0));
Z_Flags <= zero;
BE_Flags <= iCB_Flags or zero;
oGE_Flags <= not (R_Flags(3) xor iOV_Flags); --(((not zero) and (((not R_Flags(3)) and (not iOV_Flags)) or (R_Flags(3) and iOV_Flags)))) or zero;
oOV_Flags <= iOV_Flags;
U1: Mux1b_Flags port map (A_Mux => iCB_Flags, B_Mux => CY_Flags, S_Mux => OP_Flags, Y_Mux => oCB_Flags);
end Flags_TLAB2_ARCH;