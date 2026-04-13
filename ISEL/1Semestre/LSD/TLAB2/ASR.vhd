library ieee;

use ieee.std_logic_1164.all;

entity ASR is port(
	A_ASR: in std_logic_vector(3 downto 0);
	R_ASR: out std_logic_vector(3 downto 0);
	CY_ASR: out std_logic
	);
end ASR;

architecture ASR_ARCH of ASR is
begin
R_ASR(0) <= A_ASR(1);
R_ASR(1) <= A_ASR(2);
R_ASR(2) <= A_ASR(3);
R_ASR(3) <= A_ASR(3);
CY_ASR <= A_ASR(0);
end ASR_ARCH;