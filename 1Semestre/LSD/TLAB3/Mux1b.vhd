library ieee;

use ieee.std_logic_1164.all;

entity Mux1b is
port(
	A : in std_logic;
	B : in std_logic;
	S : in std_logic;
	Y : out std_logic
	);
end Mux1b;

architecture Mux1b_ARCH of Mux1b is
begin

Y <= (A and not S) or (B and S);

end Mux1b_ARCH;