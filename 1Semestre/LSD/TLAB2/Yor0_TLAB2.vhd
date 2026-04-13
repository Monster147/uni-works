library ieee;

use ieee.std_logic_1164.all;

entity Yor0_TLAB2 is port(
	A_Yor0 : in std_logic_vector(3 downto 0);
	S_Yor0 : in std_logic;
	Y_Yor0 : out std_logic_vector(3 downto 0)
	);
end Yor0_TLAB2;

architecture Yor0_TLAB2_ARCH of Yor0_TLAB2 is
begin
Y_Yor0(0) <= ('0' and not S_Yor0) or (S_Yor0 and A_Yor0(0));
Y_Yor0(1) <= ('0' and not S_Yor0) or (S_Yor0 and A_Yor0(1));
Y_Yor0(2) <= ('0' and not S_Yor0) or (S_Yor0 and A_Yor0(2));
Y_Yor0(3) <= ('0' and not S_Yor0) or (S_Yor0 and A_Yor0(3));
end Yor0_TLAB2_ARCH;