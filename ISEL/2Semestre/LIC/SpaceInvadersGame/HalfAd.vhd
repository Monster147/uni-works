library ieee;

use ieee.std_logic_1164.all;

entity HalfAd is
 port(
 A: in std_logic;
 B: in std_logic;
 S: out std_logic;
 Co: out std_logic
 );
end HalfAd;

architecture HalfAd_ARCH of HalfAd is
begin
S <= A xor B;
Co <= A and B;
end HalfAd_ARCH;
	