library ieee;

use ieee.std_logic_1164.all;

entity LSR is port(
	A_LSR: in std_logic_vector(3 downto 0);
	R_LSR: out std_logic_vector(3 downto 0);
	CY_LSR: out std_logic
	);
end LSR;

architecture LSR_ARCH of LSR is
begin
R_LSR(0) <= A_LSR(1);
R_LSR(1) <= A_LSR(2);
R_LSR(2) <= A_LSR(3);
R_LSR(3) <= '0';
CY_LSR <= A_LSR(0);
end LSR_ARCH;